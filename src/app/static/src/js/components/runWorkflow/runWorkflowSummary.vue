<template>
<div>
    <h2 id="summary-header">Summary</h2>
</div>
</template>

<script lang="ts">
import Vue from "vue"
import {RunWorkflowMetadata} from "../../utils/types";

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
}

interface Computed {
    validateStep: void
}

export default Vue.extend<unknown, Computed, unknown, Props>({
    name: "runWorkflowSummary",
    props: {
        workflowMetadata: null
    },
    computed: {
    validateStep: function () {
        /**
         * ValidateStep should enable/disable buttons once validation is complete
         */
        this.$emit("enabled", {back: false, next: false})
    }
}
})
</script>
