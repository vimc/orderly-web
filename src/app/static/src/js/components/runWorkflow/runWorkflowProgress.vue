<template>
    <p>Run workflow progress is coming soon</p>
</template>

<script lang="ts">
import Vue from 'vue'
import {WorkflowRun} from "../../utils/types";

interface Props {
    workflowRun: WorkflowRun | null
}

export default Vue.extend<unknown, unknown, unknown, Props>({
    name: "runWorkflowProgress",
    props: {
        workflowRun: null
    }
})
</script>
