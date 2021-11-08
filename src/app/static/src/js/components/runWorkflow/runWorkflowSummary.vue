<template>
    <div>
        <h2 id="summary-header">Summary</h2>
        <span>Your workflow contains {{ reportCount }}:</span>
        <ul class="styled">
            <li v-for="report in workflowMetadata.reports">
                {{ report.name }}
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata} from "../../utils/types";

    interface Props {
        workflowMetadata: RunWorkflowMetadata
    }

    interface Computed {
        validateStep: void
    }

    export default Vue.extend<unknown, Computed, unknown, Props>({
        name: "runWorkflowSummary",
        props: {
            workflowMetadata: {
                required: true,
                type: Object
            }
        },
        computed: {
            reportCount() {
                const num = this.workflowMetadata?.reports?.length;
                if (num == 1) {
                    return "1 report"
                } else {
                    return `${num} reports`
                }
            }
        },
        mounted() {
            // the summary page is "valid" by default
            this.$emit("valid", true)
        }
    })
</script>
