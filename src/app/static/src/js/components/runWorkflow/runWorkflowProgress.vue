<template>
    <div>
        <div
            class="container mt-3"
            id="workflow-progress-container"
            v-if="workflowRunSummaries"
        >
            <div class="row mb-3">
                <label for="workflows" class="form-label col">Workflow</label>
                <div class="col-10 px-0">
                    <v-select
                        :options="workflowRunSummaries"
                        label="name"
                        :reduce="(label) => label.key"
                        name="workflows"
                        id="workflows"
                        v-model="selectedWorkflowKey"
                        placeholder="Select workflow or search by name..."
                    >
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
                            <a :href="reportVersionHref(report.name, report.version)">
                                {{ report.name }}
                            </a>
                        </td>
                        <td v-else class="p-2">{{ report.name }}</td>
                        <td :class="statusColour(report.status)" class="p-2">
                            {{ interpretStatus(report.status) }}
                        </td>
                        <td v-if="report.date" class="p-2">{{ formatDate(report.date) }}</td>
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
        <error-info
            :default-message="defaultMessage"
            :api-error="error"
        ></error-info>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import vSelect from "vue-select";
import { api } from "../../utils/api";
import {longTimestamp, workflowRunDetailsToMetadata} from "../../utils/helpers.ts";
import ErrorInfo from "../errorInfo.vue";
import {
    WorkflowRunSummary,
    WorkflowRunStatus,
} from "../../utils/types";
import { buildFullUrl } from "../../utils/api";
import {SELECTED_RUNNING_REPORT_KEY, SELECTED_RUNNING_WORKFLOW_KEY, session} from "../../utils/session";

interface Data {
    workflowRunSummaries: null | WorkflowRunSummary[];
    selectedWorkflowKey: null | string;
    workflowRunStatus: null | WorkflowRunStatus;
    error: string;
    defaultMessage: string;
    pollingTimer: null | number;
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
}

const failStates = ["error", "orphan", "impossible", "missing", "interrupted"]

export default Vue.extend<Data, Methods, unknown, unknown>({
    name: "runWorkflowProgress",
    components: {
        ErrorInfo,
        vSelect,
    },
    data() {
        return {
            workflowRunSummaries: null,
            selectedWorkflowKey: null,
            workflowRunStatus: null,
            error: "",
            defaultMessage: "",
            pollingTimer: null
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
            if (status === "success") {
                return "Complete";
            } else if (
                failStates.includes(status)
            ) {
                return "Failed";
            } else {
                return status.charAt(0).toUpperCase() + status.slice(1);
            }
        },
    },
    watch: {
        selectedWorkflowKey() {
            if (this.selectedWorkflowKey) {
                this.getWorkflowRunStatus(this.selectedWorkflowKey);
                this.startPolling();
            } else {
                this.workflowRunStatus = null;
            }
        },
        workflowRunStatus: {
            handler(workflowRun) {
                const interpretStatus = this.interpretStatus(workflowRun.status)
                if (interpretStatus === "Failed" || interpretStatus === "Complete") {
                    this.stopPolling();
                }
            }
        }
    },
    mounted() {
        this.getWorkflowRunSummaries();
    },
    beforeDestroy() {
        this.stopPolling();
    }
});
</script>
