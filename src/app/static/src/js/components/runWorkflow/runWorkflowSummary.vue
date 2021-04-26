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

export default Vue.extend<unknown, unknown, unknown, Props>({
    name: "runWorkflowSummary",
    props: {
        workflowMetadata: null
    }
})
</script>
