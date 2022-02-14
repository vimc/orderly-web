<template>
    <div id="summary-header">
        <runWorflowSummaryHeader :workflow-summary="workflowSummary"></runWorflowSummaryHeader>
        <div v-if="hasDependenciesLength">
            <workflow-summary-reports :workflow-summary="workflowSummary"/>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata, WorkflowSummaryResponse} from "../../../utils/types";
    import runWorflowSummaryHeader from "./runWorkflowSummaryHeader.vue";
    import ErrorInfo from "../../../../js/components/errorInfo.vue";
    import {api} from "../../../utils/api";
    import WorkflowSummaryReports from "./workflowSummaryReports.vue"

    interface Props {
        workflowMetadata: RunWorkflowMetadata
    }

    interface Data {
        workflowSummary: WorkflowSummaryResponse | null,
        error: string,
        defaultMessage: string,
    }

    interface Computed {
        hasDependenciesLength: boolean;
    }

    interface Methods {
        getReportWorkflowSummary: () => void;
    }

    export default Vue.extend<unknown, Methods, Computed, Props>({
        name: "runWorkflowSummary",
        components: {
            WorkflowSummaryReports,
            runWorflowSummaryHeader,
            ErrorInfo
        },
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
        computed: {
            hasDependenciesLength() {
                return !!this.workflowSummary
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
            this.getReportWorkflowSummary();
            this.$emit("valid", true)
        }
    })
</script>
