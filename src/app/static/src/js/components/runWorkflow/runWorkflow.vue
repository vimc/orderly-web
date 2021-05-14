<template>
    <div class="container">
        <run-workflow-create v-if="!workflowStarted"
                             @rerun="handleRerun"
                             @create="handleCreate"
                             @clone="handleClone">
        </run-workflow-create>
        <workflow-wizard v-if="workflowStarted"
                         :steps="stepComponents"
                         :backButtonVisible="backButtonVisible"
                         @cancel="handleCancel"
                         @complete="handleComplete"
                         :run-workflow-metadata="runWorkflowMetadata">
        </workflow-wizard>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import workflowWizard from "../workflowWizard/workflowWizard.vue";
    import {RunWorkflowMetadata, Steps} from "../../utils/types"
    import runWorkflowCreate from "./runWorkflowCreate.vue";

    interface Data {
        runWorkflowMetadata: RunWorkflowMetadata | null
        workflowStarted: boolean
        backButtonVisible: boolean
        stepComponents: Steps[]
    }

    interface Methods {
        handleCancel: () => void
        handleRerun: () => void
        handleCreate: () => void
        handleClone: () => void
        handleComplete: () => void
    }

export default Vue.extend<Data, Methods, unknown, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            runWorkflowMetadata: null,
            workflowStarted: false,
            backButtonVisible: true,
            stepComponents: []
        }
    },
    methods: {
        handleRerun: function () {
            // can set metadata require to rerun here
            this.stepComponents = [{name: "run", component: "runWorkflowRun"}]
            this.backButtonVisible = false
            this.workflowStarted = true
        },
        handleCreate: function () {
            this.stepComponents = [
                {name: "report", component: "runWorkflowReport"},
                {name: "run", component: "runWorkflowRun"},
            ]
            this.backButtonVisible = true
            this.workflowStarted = true
        },
        handleClone: function () {
            //Can set metadata required for clone here
            this.stepComponents = [
                {name: "report", component: "runWorkflowReport"},
                {name: "run", component: "runWorkflowRun"},
            ]
            this.backButtonVisible = true
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