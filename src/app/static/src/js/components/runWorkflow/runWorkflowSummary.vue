<template>
    <div>
        <runWorflowSummaryHeader :dependencies="dependencies"></runWorflowSummaryHeader>
        <!-- <span>Your workflow contains {{ reportCount }}:</span>
        <ul class="styled">
            <li v-for="report in workflowMetadata.reports">
                {{ report.name }}
            </li>
        </ul> -->
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata} from "../../utils/types";
    import runWorflowSummaryHeader from "./runWorkflowSummaryHeader.vue";
    import {Dependency} from "../../utils/types";
    import {api} from "../../utils/api";

    interface Props {
        workflowMetadata: RunWorkflowMetadata
    }

    interface Data {
        dependencies: Dependency | null
    }

    interface Computed {
        // validateStep: void
    }

    interface Methods {
        getReportDependencies: () => void;
    }

    export default Vue.extend<unknown, Methods, Computed, Props>({
        name: "runWorkflowSummary",
        props: {
            workflowMetadata: {
                required: true,
                type: Object
            }
        },
        data() {
            return {
                dependencies: null
            }
        },
        computed: {
            // reportCount() {
            //     const num = this.workflowMetadata?.reports?.length;
            //     if (num == 1) {
            //         return "1 report"
            //     } else {
            //         return `${num} reports`
            //     }
            // }
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
            },
        },
        mounted() {
            // the summary page is "valid" by default
            this.getReportDependencies();
            this.$emit("valid", true)
        },
        watch: {
            dependencies(){
                console.log("dependencies", this.dependencies)
            }
        },
        components: {
            runWorflowSummaryHeader
        }
    })
</script>
