<template>
    <div id="summary-header">
        <div v-if="hasDependenciesLength">
            <report-parameter :dependencies="dependencies" :git-commit.sync="workflowMetadata.git_commit"/>
            <div v-if="error"><p class="row mt-3 justify-content-center col-8 text-danger">{{ error }}</p></div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata, Dependency} from "../../../utils/types";
    import {api} from "../../../utils/api";
    import ReportParameter from "./reportParameter.vue"

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
        dependencies: Dependency | null
        error: string
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runWorkflowSummary",
        components: {
            ReportParameter
        },
        data() {
            return {
                dependencies: null,
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
                return !!this.dependencies
            }
        },
        methods: {
            getReportDependencies() {
                api.post(`/workflows/summary`, {
                    reports: this.workflowMetadata.reports,
                    ref: this.workflowMetadata.git_commit
                })
                    .then(({data}) => {
                        this.dependencies = data.data;
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
