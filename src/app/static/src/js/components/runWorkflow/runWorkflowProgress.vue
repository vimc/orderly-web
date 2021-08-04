<template>
    <p>Run workflow progress is coming soon</p>
</template>

<script lang="ts">
import Vue from 'vue'

interface Data {
    workflowRunSummaries: null | WorkflowRunSummary[];
    selectedWorkflowKey: null | string;
    workflowRunStatus: null | WorkflowRunStatus;
    error: string;
    defaultMessage: string;
    polling: string;
}

interface Methods {
    getWorkflowRunSummaries: () => void;
    getWorkflowRunStatus: (key: string) => void;
    formatDate: (date: string) => string;
    reportVersionHref: (name: string, version: string) => string;
    statusColour: (status: string) => string;
    interpretStatus: (status: string) => string;
    startPolling: () => void
    stopPolling: () => void
}

interface Props {
    workflowMetadata: RunWorkflowMetadata | null;
}

const failStates = ["error", "orphan", "impossible", "missing", "interrupted"]

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "runWorkflowProgress",
    components: {
        ErrorInfo,
        vSelect,
    },
    props: {
        workflowMetadata: null,
    },
    data() {
        return {
            workflowRunSummaries: null,
            selectedWorkflowKey: null,
            workflowRunStatus: null,
            error: "",
            defaultMessage: "",
            polling: ""
        };
    },
    methods: {
        startPolling() {
            this.polling = setInterval(() => {
                this.getWorkflowRunStatus(this.selectedWorkflowKey)
            },1500)
        },
        stopPolling() {
            clearInterval(this.polling)
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
                this.startPolling()
            } else {
                this.workflowRunStatus = null;
            }
        },
        workflowRunStatus: {
            handler(status) {
                if (status === "success" || status === "error") {
                    this.stopPolling()
                }
            },
            deep: true
        }
    },
    mounted() {
        this.getWorkflowRunSummaries();
    },
});
</script>
