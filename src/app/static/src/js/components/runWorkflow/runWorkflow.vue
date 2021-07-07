<template>
    <div class="container">
        <run-workflow-create v-if="!workflowStarted"
                             @rerun="handleRerun"
                             @create="handleCreate"
                             @clone="handleClone">
        </run-workflow-create>
        <workflow-wizard v-if="workflowStarted"
                         :steps="stepComponents"
                         :submit-label="toggleFinalStepNextTo"
                         @cancel="handleCancel"
                         @complete="handleComplete"
                         :initial-run-workflow-metadata="runWorkflowMetadata">
        </workflow-wizard>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import workflowWizard from "../workflowWizard/workflowWizard.vue";
    import {RunWorkflowMetadata, Step} from "../../utils/types"
    import runWorkflowCreate from "./runWorkflowCreate.vue";

    interface Data {
        runWorkflowMetadata: RunWorkflowMetadata | null
        workflowStarted: boolean
        stepComponents: Step[]
        toggleFinalStepNextTo: string | null
    }

    interface Methods {
        handleCancel: () => void
        handleRerun: (data: Event) => void
        handleCreate: (data: Event) => void
        handleClone: (data: Event) => void
        handleComplete: () => void
    }

export default Vue.extend<Data, Methods, unknown, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            runWorkflowMetadata: null,
            workflowStarted: false,
            stepComponents: [],
            toggleFinalStepNextTo: "Run workflow"
        }
    },
    methods: {
        handleRerun: function (data) {
            this.runWorkflowMetadata = data
            this.stepComponents = [{name: "run", component: "runWorkflowRun"}]
            this.workflowStarted = true
        },
        handleCreate: function (data) {
            this.runWorkflowMetadata = data
            this.stepComponents = [
                {name: "report", component: "runWorkflowReport"},
                {name: "run", component: "runWorkflowRun"},
            ]
            this.workflowStarted = true
        },
        handleClone: function (data) {
            this.runWorkflowMetadata = data
            this.stepComponents = [
                {name: "report", component: "runWorkflowReport"},
                {name: "run", component: "runWorkflowRun"},
            ]
            this.workflowStarted = true
        },
        handleCancel: function () {
            this.workflowStarted = false
        },
        handleComplete: function () {
            //handle submitted report here
        }
    },
    components: {
        workflowWizard,
        runWorkflowCreate
    }
})
</script>
