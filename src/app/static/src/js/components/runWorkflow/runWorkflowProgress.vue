<template>
    <p>Run workflow progress is coming soon</p>
</template>

<script lang="ts">
import Vue from 'vue'
import {RunWorkflowMetadata} from "../../utils/types";

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
}

export default Vue.extend<unknown, unknown, unknown, Props>({
    name: "runWorkflowProgress",
    props: {
        workflowMetadata: null
    }
})
</script>