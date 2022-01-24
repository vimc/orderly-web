<template>
    <div>
        <runWorflowSummaryHeader :workflow-summary="workflowSummary"></runWorflowSummaryHeader>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata} from "../../utils/types";
    import runWorflowSummaryHeader from "./runWorkflowSummaryHeader.vue";
    import ErrorInfo from "../../../js/components/errorInfo.vue";
    import {WorkflowSummaryEndpoint} from "../../utils/types";
    import {api} from "../../utils/api";

    interface Props {
        workflowMetadata: RunWorkflowMetadata
    }

    interface Data {
        workflowSummary: WorkflowSummaryEndpoint | null,
        error: string,
        defaultMessage: string,
    }

    interface Methods {
        getReportWorkflowSummary: () => void;
    }

    export default Vue.extend<unknown, Methods, unknown, Props>({
        name: "runWorkflowSummary",
        props: {
            workflowMetadata: {
                required: true,
                type: Object
            }
        },
        data() {
            return {
                workflowSummary: null,
                error: "",
                defaultMessage: "",
            }
        },
        methods: {
            getReportWorkflowSummary() {
                api.post(`/workflows/summary`, {
                    reports: this.workflowMetadata.reports,
                    ref: this.workflowMetadata.git_commit
                })
                    .then(({data}) => {
                        this.workflowSummary = data.data;
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving the workflow summary";
                    })
            },
        },
        mounted() {
            // the summary page is "valid" by default
            this.getReportWorkflowSummary();
            this.$emit("valid", true)
        },
        components: {
            runWorflowSummaryHeader,
            ErrorInfo
        }
    })
</script>
