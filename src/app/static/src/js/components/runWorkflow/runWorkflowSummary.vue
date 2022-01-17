<template>
    <div>
        <runWorflowSummaryHeader :workflow-summary="workflowSummary"></runWorflowSummaryHeader>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata} from "../../utils/types";
    import runWorflowSummaryHeader from "./runWorkflowSummaryHeader.vue";
    import {WorkflowSummary} from "../../utils/types";
    import {api} from "../../utils/api";

    interface Props {
        workflowMetadata: RunWorkflowMetadata
    }

    interface Data {
        workflowSummary: WorkflowSummary | null
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
                workflowSummary: null
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
                    })
                    .catch((error) => {
                        this.error = error;
                    })
            },
        },
        mounted() {
            // the summary page is "valid" by default
            this.getReportWorkflowSummary();
            this.$emit("valid", true)
        },
        components: {
            runWorflowSummaryHeader
        }
    })
</script>
