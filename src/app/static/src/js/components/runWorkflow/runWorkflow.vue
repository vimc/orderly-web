<template>
    <div class="container">
        <workflow-wizard :run-workflow-metadata="runWorkflowMetadata"></workflow-wizard>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import workflowWizard from "../workflowWizard/workflowWizard.vue";
import {RunWorkflowMetadata} from "../../utils/types"

interface Data {
    runWorkflowMetadata: RunWorkflowMetadata | null
}

export default Vue.extend<Data, unknown, unknown, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            runWorkflowMetadata: {placeholder: "Shared state works"},
        }
    },
    components: {
        workflowWizard
    }
})
</script>