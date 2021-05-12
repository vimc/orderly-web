<template>
    <div class="container">
        <run-workflow-create v-if="!action" @jump="getAction"></run-workflow-create>
        <workflow-wizard v-if="action" :entry-step="entryStep" @cancel=cancel
                         :run-workflow-metadata="runWorkflowMetadata">
        </workflow-wizard>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import workflowWizard from "../workflowWizard/workflowWizard.vue";
import {RunWorkflowMetadata} from "../../utils/types"
import runWorkflowCreate from "./runWorkflowCreate.vue";

interface Data {
    runWorkflowMetadata: RunWorkflowMetadata | null
    action: boolean,
    entryStep: string | null
}

interface Methods {
    getAction: (action) => void
    cancel: () => void
}

export default Vue.extend<Data, Methods, unknown, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            runWorkflowMetadata: null,
            action: false,
            entryStep: null
        }
    },
    methods: {
        getAction: function (action) {
            if (action == "clone") {
                /**
                 * Pre-population of runWorkflowMetadata can happen at this stage.
                 */
            }
            this.entryStep = action === "rerun" ? "run" : "report";
            this.action = true
        },
        cancel: function () {
            this.action = false
        }
    },
    components: {
        workflowWizard,
        runWorkflowCreate
    }
})
</script>