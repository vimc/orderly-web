<template>
    <div>
        <div
            class="container mt-3"
            id="workflow-progress-container"
            v-if="workflowRunSummaries">
            <div class="row mb-3">
                <label for="workflows" class="form-label col">Workflow</label>
                <div class="col-10 px-0">
                    <v-select :options="workflowRunSummaries"
                              label="name"
                              :reduce="(label) => label.key"
                              name="workflows"
                              id="workflows"
                              v-model="selectedWorkflowKey"
                              :clearable = "false"
                              placeholder="Select workflow or search by name...">
                        <template #option="{ name, date }">
                            <div>
                                {{ name }}
                                <span style="opacity: 0.5; float: right">
                                    {{ formatDate(date) }}
                                </span>
                            </div>
                        </template>
                    </v-select>
                </div>
            </div>
            <div class="row" v-if="workflowRunStatus" id="workflow-table">
                <label class="form-label col">Reports</label>
                <table class="table-bordered col-10">
                    <tr>
                        <th class="p-2">Report</th>
                        <th v-if="anyParams" class="p-2">Parameters</th>
                        <th class="p-2">Status</th>
                        <th class="p-2">Logs</th>
                    </tr>
                    <tr v-for="(report, index) in workflowRunStatus.reports" :key="report.key">
                        <td v-if="report.status === 'success'" class="p-2">
                            <a class="report-version-link" :href="reportVersionHref(report.name, report.version)">
                                {{ report.name }}
                            </a>
                        </td>
                        <td v-else class="p-2">{{ report.name }}</td>
                        <td v-if="anyParams" class="p-2">
                            <run-workflow-parameters :report="workflowSummary.reports[index]"></run-workflow-parameters>
                        </td>
                        <td :class="statusColour(report.status)" class="p-2">
                            {{ interpretStatus(report.status) }}
                        </td>
                        <td v-if="report.date" class="p-2">{{ formatDate(report.date) }}</td>
                        <td class="p-2">
                            <a v-if="viewLogLinkVisible(report.status)"
                               class="report-log-link"
                               href="#"
                               @click="showReportLog(report.key)">
                                View log
                            </a>
                        </td>
                    </tr>
                </table>
            </div>
            <div v-if="selectedWorkflowKey" class="row justify-content-end mt-3">
                <button id="rerun" class="button mr-3" type="button" @click="rerun" :disabled="!workflowMetadata">
                    Re-run workflow
                </button>
                <!-- Cancel button to be implemented in mrc-2549 -->
                <button class="btn btn-secondary" type="button" v-if="false">
                    Cancel workflow
                </button>
            </div>
        </div>
        <p v-else>No workflows to show</p>
        <error-info :default-message="defaultMessage"
                    :api-error="error">
        </error-info>
        <workflow-report-log-dialog id="report-log-dialog"
                                    :report-key="showLogForReportKey"
                                    :workflow-key="selectedWorkflowKey"
                                    @close="closeReportLogDialog">
        </workflow-report-log-dialog>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import vSelect from "vue-select";
import { api } from "../../utils/api";
import {longTimestamp, workflowRunDetailsToMetadata} from "../../utils/helpers.ts";
import ErrorInfo from "../errorInfo.vue";
import WorkflowReportLogDialog from "./workflowReportLogDialog.vue";
import {
    WorkflowRunSummary,
    WorkflowRunStatus,
    RunWorkflowMetadata,
    WorkflowRunReportStatus,
    WorkflowSummaryResponse,
    WorkflowReportWithDependencies
} from "../../utils/types";
import { buildFullUrl } from "../../utils/api";
import runWorkflowParameters from "./runWorkflowParameters.vue"

interface Data {
    workflowRunSummaries: null | WorkflowRunSummary[];
    selectedWorkflowKey: null | string;
    workflowRunStatus: null | WorkflowRunReportStatus;
    error: string;
    defaultMessage: string;
    pollingTimer: null | number;
    showLogForReportKey: string | null;
    workflowMetadata: RunWorkflowMetadata | null;
    workflowSummary: WorkflowSummaryResponse | null;
}

interface Computed {
    anyParams: boolean
}

interface Methods {
    getWorkflowRunSummaries: () => void;
    getWorkflowRunStatus: (key: string) => void;
    formatDate: (date: string) => string;
    reportVersionHref: (name: string, version: string) => string;
    statusColour: (status: string) => string;
    interpretStatus: (status: string) => string;
    rerun: () => void;
    startPolling: () => void;
    stopPolling: () => void;
    viewLogLinkVisible: (status: string) => boolean;
    showReportLog: (reportKey: string) => void;
    closeReportLogDialog: () => void;
    hasParams: (report: WorkflowReportWithDependencies) => boolean
    getWorkflowMetadata: () => void;
    getWorkflowSummary: () => void;
}

interface Props {
    initialSelectedWorkflow: string;
}

const failStates = ["error", "orphan", "impossible", "missing", "interrupted"];
const notStartedStates = ["queued", "deferred", "impossible", "missing"];
const nonFailStateMessages = {
    "success": "Complete",
    "impossible": "Dependency failed",
    "deferred": "Waiting for dependency"
};

export default Vue.extend<Data, Methods, Computed, Props>({
    name: "runWorkflowProgress",
    components: {
        ErrorInfo,
        WorkflowReportLogDialog,
        vSelect,
        runWorkflowParameters
    },
    props: {
        initialSelectedWorkflow: {
            type: String,
            required: true
        },
    },
    data() {
        return {
            workflowRunSummaries: null,
            selectedWorkflowKey: null,
            workflowRunStatus: null,
            error: "",
            defaultMessage: "",
            pollingTimer: null,
            showLogForReportKey: null,
            workflowMetadata: null,
            workflowSummary: null
        };
    },
    computed: {
        anyParams() {
            return !!this.workflowSummary?.reports.some(report => this.hasParams(report))
        }
    },
    methods: {
        startPolling() {
            if (!this.pollingTimer) {
                this.pollingTimer = setInterval(() => this.getWorkflowRunStatus(this.selectedWorkflowKey), 1500);
            }
        },
        stopPolling() {
            if(this.pollingTimer) {
                clearInterval(this.pollingTimer)
                this.pollingTimer = null
            }
        },
        getWorkflowRunSummaries() {
            api.get("/workflows")
                .then(({ data }) => {
                    this.workflowRunSummaries = data.data;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching the workflows";
                });
        },
        getWorkflowRunStatus(key) {
            api.get(`/workflows/${key}/status`)
                .then(({ data }) => {
                    this.workflowRunStatus = data.data;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching the workflow reports";
                });
        },
        hasParams(report) {
            return (report.param_list && report.param_list.length > 0) ||
                (report.default_param_list && report.default_param_list.length > 0)
        },
        rerun() {
            this.$emit("rerun", this.workflowMetadata);
        },
        getWorkflowMetadata(){
            api.get(`/workflows/${this.selectedWorkflowKey}/`)
                .then(({data}) => {
                    this.workflowMetadata = workflowRunDetailsToMetadata(data.data)
                    this.getWorkflowSummary()
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching workflow details";
                });
        },
        getWorkflowSummary() {
            const commit = this.workflowMetadata.git_commit ? `?commit=${this.workflowMetadata.git_commit}` : '';
            api.post(`/workflows/summary/${commit}`, {
                reports: this.workflowMetadata.reports,
                ref: this.workflowMetadata.git_commit
            })
                .then(({data}) => {
                    this.workflowSummary = data.data
                    this.error = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching a report's dependencies";
                })
        },
        formatDate(date) {
            return longTimestamp(new Date(date));
        },
        reportVersionHref(name, version) {
            const url = `/report/${name}/${version}/`;
            return buildFullUrl(url);
        },
        statusColour(status) {
            if (["queued", "running"].includes(status)) {
                return "text-secondary";
            } else if (failStates.includes(status)) {
                return "text-danger";
            } else {
                return "";
            }
        },
        interpretStatus(status) {
            if (Object.keys(nonFailStateMessages).includes(status)) {
                return nonFailStateMessages[status]
            } else if (
                failStates.includes(status)
            ) {
                return "Failed";
            } else {
                return status.charAt(0).toUpperCase() + status.slice(1);
            }
        },
        viewLogLinkVisible(status) {
            return !notStartedStates.includes(status);
        },
        showReportLog(reportKey) {
            this.showLogForReportKey = reportKey;
        },
        closeReportLogDialog() {
            this.showLogForReportKey = null;
        }
    },
    watch: {
        selectedWorkflowKey() {
            this.$emit("set-selected-workflow-key", this.selectedWorkflowKey)
            if (this.selectedWorkflowKey) {
                this.getWorkflowRunStatus(this.selectedWorkflowKey);
                this.getWorkflowMetadata();
                this.startPolling();
            } else {
                this.workflowRunStatus = null;
                this.workflowMetadata = null;
            }
        },
        workflowRunStatus(newWorkflowRunStatus) {
            const interpretStatus = this.interpretStatus(newWorkflowRunStatus.status)
            if (interpretStatus === "Failed" || interpretStatus === "Complete") {
                this.stopPolling();
            }
        },
    },
    mounted() {
        this.getWorkflowRunSummaries();
        this.selectedWorkflowKey = this.initialSelectedWorkflow;
        this.getWorkflowMetadata();
    },
    beforeDestroy() {
        this.stopPolling();
    }
});
</script>
