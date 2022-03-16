<template>
    <div>
        <div
            v-if="workflowRunSummaries"
            id="workflow-progress-container"
            class="container mt-3">
            <div class="row mb-3">
                <label for="workflows" class="form-label col">Workflow</label>
                <div class="col-10 px-0">
                    <v-select id="workflows"
                              v-model="selectedWorkflowKey"
                              :options="workflowRunSummaries"
                              label="name"
                              :reduce="(label) => label.key"
                              name="workflows"
                              :clearable="false"
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
            <div v-if="workflowRunStatus" id="workflow-table" class="row">
                <label class="form-label col">Reports</label>
                <run-workflow-table class="col-10" :workflow-run-status="workflowRunStatus"
                                    :workflow-summary="workflowSummary"
                                    @show-report-log="showReportLog"></run-workflow-table>
            </div>
            <div v-if="selectedWorkflowKey" class="row justify-content-end mt-3">
                <button id="rerun" class="button mr-3" type="button" :disabled="!workflowMetadata" @click="rerun">
                    Re-run workflow
                </button>
                <!-- Cancel button to be implemented in mrc-2549 -->
                <button v-if="false" class="btn btn-secondary" type="button">
                    Cancel workflow
                </button>
            </div>
        </div>
        <p v-else>
            No workflows to show
        </p>
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
    import {api} from "../../utils/api";
    import {workflowRunDetailsToMetadata, formatDate, interpretStatus} from "../../utils/helpers.ts";
    import ErrorInfo from "../errorInfo.vue";
    import WorkflowReportLogDialog from "./workflowReportLogDialog.vue";
    import {
        WorkflowRunSummary,
        WorkflowRunStatus,
        RunWorkflowMetadata,
        WorkflowSummaryResponse,
    } from "../../utils/types";
    import runWorkflowTable from "./runWorkflowTable.vue"

    interface Data {
        workflowRunSummaries: null | WorkflowRunSummary[];
        selectedWorkflowKey: null | string;
        workflowRunStatus: null | WorkflowRunStatus;
        error: string;
        defaultMessage: string;
        pollingTimer: null | number;
        showLogForReportKey: string | null;
        workflowMetadata: RunWorkflowMetadata | null;
        workflowSummary: WorkflowSummaryResponse | null;
    }

    interface Methods {
        getWorkflowRunSummaries: () => void;
        getWorkflowRunStatus: (key: string) => void;
        formatDate: (date: string) => string;
        rerun: () => void;
        startPolling: () => void;
        stopPolling: () => void;
        showReportLog: (reportKey: string) => void;
        closeReportLogDialog: () => void;
        getWorkflowMetadata: () => void;
        getWorkflowSummary: () => void;
    }

    interface Props {
        initialSelectedWorkflow: string;
    }

    export default Vue.extend<Data, Methods, unknown, Props>({
        name: "RunWorkflowProgress",
        components: {
            ErrorInfo,
            WorkflowReportLogDialog,
            vSelect,
            runWorkflowTable
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
                // gives the status of each report
                workflowRunStatus: null,
                error: "",
                defaultMessage: "",
                pollingTimer: null,
                showLogForReportKey: null,
                // contains git_commit and report parameters (needed to get the workflow summary)
                workflowMetadata: null,
                // contains the params and default params for each report
                workflowSummary: null
            };
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
                    this.workflowSummary = null;
                }
            },
            workflowRunStatus(newWorkflowRunStatus) {
                const interpretStatusState = interpretStatus(newWorkflowRunStatus.status)
                if (interpretStatusState === "Failed" || interpretStatusState === "Complete") {
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
        },
        methods: {
            startPolling() {
                if (!this.pollingTimer) {
                    this.pollingTimer = setInterval(() => this.getWorkflowRunStatus(this.selectedWorkflowKey), 1500);
                }
            },
            stopPolling() {
                if (this.pollingTimer) {
                    clearInterval(this.pollingTimer)
                    this.pollingTimer = null
                }
            },
            getWorkflowRunSummaries() {
                api.get("/workflows")
                    .then(({data}) => {
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
                    .then(({data}) => {
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
                this.$emit("rerun", this.workflowMetadata);
            },
            getWorkflowMetadata() {
                if (this.selectedWorkflowKey) {
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
                }
            },
            formatDate(date) {
                return formatDate(date)
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
            showReportLog(reportKey) {
                this.showLogForReportKey = reportKey;
            },
            closeReportLogDialog() {
                this.showLogForReportKey = null;
            }
        }
    });
</script>
