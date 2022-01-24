<template>
    <div id="summary-header">
        <div v-if="hasDependenciesLength">
            <workflow-summary-reports :workflow-summary="workflowSummary"/>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata, WorkflowSummary} from "../../../utils/types";
    import {api} from "../../../utils/api";
    import WorkflowSummaryReports from "./workflowSummaryReports.vue"

    interface Props {
        workflowMetadata: RunWorkflowMetadata;
    }

    interface Computed {
        hasDependenciesLength: boolean;
    }

    interface Methods {
        getReportDependencies: () => void;
    }

    interface Data {
        workflowSummary: WorkflowSummary | null
        error: string
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runWorkflowSummary",
        components: {
            WorkflowSummaryReports
        },
        data() {
            return {
                workflowSummary: null,
                error: ""
            }
        },
        props: {
            workflowMetadata: {
                required: true,
                type: Object
            }
        },
        computed: {
            hasDependenciesLength() {
                return !!this.workflowSummary
            }
        },
        methods: {
            getReportDependencies() {
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
            }
        },
        mounted() {
            this.getReportDependencies();
            this.$emit("valid", true)
        }
    })
</script>
