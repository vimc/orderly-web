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
                    <tr v-for="report in workflowRunStatus.reports">
                        <td v-if="report.status === 'success'" class="p-2">
                            <a class="report-version-link" :href="reportVersionHref(report.name, report.version)">
                                {{ report.name }}
                            </a>
                        </td>
                        <td v-else class="p-2">{{ report.name }}</td>
                        <td>
                            <tr>Parameters</tr>
                            <tr>param 1: name 1</tr>
                            <tr>param 2: name 2</tr>
                            <tr>show default...</tr>
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
                <button id="rerun" class="button mr-3" type="button" @click="rerun">
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
                                    :report-key=showLogForReportKey
                                    :workflow-key=selectedWorkflowKey
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
    RunWorkflowMetadata
} from "../../utils/types";
import { buildFullUrl } from "../../utils/api";
import {SELECTED_RUNNING_REPORT_KEY, SELECTED_RUNNING_WORKFLOW_KEY, session} from "../../utils/session";
import runWorkflowMixin from "./runWorkflowMixin.ts";
import {WorkflowSummaryResponse, Parameter} from "../../utils/types";

interface Data {
    workflowRunSummaries: null | WorkflowRunSummary[];
    selectedWorkflowKey: null | string;
    workflowRunStatus: null | WorkflowRunStatus;
    error: string;
    defaultMessage: string;
    pollingTimer: null | number;
    runWorkflowMetadata: RunWorkflowMetadata | null
    workflowSummary: WorkflowSummaryResponse | null
    showLogForReportKey: string | null;
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
    getReportWorkflowSummary: () => void;
    getWorkflowDetails: () => void
    showDefaultParameters: (reportName: string) => Parameter | null;
    getDefaultParametersError: (reportName: string) => string
    viewLogLinkVisible: (status: string) => boolean;
    showReportLog: (reportKey: string) => void;
    closeReportLogDialog: () => void;
}

interface Props {
    initialSelectedWorkflow: string;
}

// interface WorkflowReportWithDependency {
//     name: string,
//     instance?: string
//     params?: Record<string, string>,
//     depends_on?: string[]
// }

// export interface WorkflowSummary {
//     missing_dependencies: Record<string, string[]>,
//     reports: WorkflowReportWithDependency[],
//     refs: string
// }

const failStates = ["error", "orphan", "impossible", "missing", "interrupted"];
const notStartedStates = ["queued", "deferred", "impossible", "missing"];
const nonFailStateMessages = {
    "success": "Complete",
    "impossible": "Dependency failed",
    "deferred": "Waiting for dependency"
};

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "runWorkflowProgress",
    components: {
        ErrorInfo,
        WorkflowReportLogDialog,
        vSelect,
    },
    mixins: [runWorkflowMixin],
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
            workflowSummary: null,
            runWorkflowMetadata: null,
            showLogForReportKey: null
        };
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
        getWorkflowDetails() {
            api.get(`/workflows/${this.selectedWorkflowKey}/`)
                .then(({data}) => {
                    this.runWorkflowMetadata = data.data
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error
                    this.defaultMessage = "An error occurred while retrieving workflow details";
                })
        },
        getReportWorkflowSummary() {
            console.log("before workflow called", {
                    reports: this.runWorkflowMetadata.reports,
                    ref: this.runWorkflowMetadata.git_commit
                })
            api.post(`/workflows/summary`, {
                reports: this.runWorkflowMetadata.reports,
                ref: this.runWorkflowMetadata.git_commit
            })
                .then(({data}) => {
                    this.workflowSummary = data.data;
                    this.error = "";
                    console.log("workflowsummary", this.workflowSummary)
                })
                .catch((error) => {
                    this.error = error;
                })
        },
        rerun() {
            api.get(`/workflows/${this.selectedWorkflowKey}/`)
                .then(({data}) => {
                    const reportMetadata = workflowRunDetailsToMetadata(data.data)
                    this.$emit("rerun", reportMetadata);
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching workflow details";
                });
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
        showDefaultParameters(reportName) {
            console.log("default params 1", this.defaultParams?.find(data => data.reportName === reportName)?.params || null)
            return this.defaultParams?.find(data => data.reportName === reportName)?.params || null
        },
        getDefaultParametersError(reportName) {
            return this.defaultParamsErrors?.find(error => error.reportName === reportName) || ""
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
                this.getWorkflowDetails();
                this.startPolling();
            } else {
                this.workflowRunStatus = null;
                this.runWorkflowMetadata = null;
            }
        },
        workflowRunStatus(newWorkflowRunStatus) {
            const interpretStatus = this.interpretStatus(newWorkflowRunStatus.status)
            if (interpretStatus === "Failed" || interpretStatus === "Complete") {
                this.stopPolling();
            }
            // console.log("workflowRunStatus", this.workflowRunStatus)
            // if (this.workflowRunStatus){
            //     this.getReportWorkflowSummary();
            // }
        },
        runWorkflowMetadata(){
            if (this.runWorkflowMetadata){
                console.log("runWorkflowMetadata", this.runWorkflowMetadata)
                this.getReportWorkflowSummary()
                // this.getDefaultParameters(this.workflowSummary, this.workflowSummary.git_commit)
            }
        },
        defaultParams(){
            console.log("defaultParams", this.defaultParams)
        }
    },
    mounted() {
        this.getWorkflowRunSummaries();
        // this.getReportWorkflowSummary();
        this.selectedWorkflowKey = this.initialSelectedWorkflow;
    },
    beforeDestroy() {
        this.stopPolling();
    }
});
</script>
