<template>
    <div class="container">
        <run-workflow-create v-if="!workflowStarted"
                             @rerun="handleRerun"
                             @create="handleCreate"
                             @clone="handleClone">
        </run-workflow-create>
        <workflow-wizard v-if="workflowStarted"
                         :entry-step="entryStep"
                         :backButtonVisible="backButtonVisible"
                         @cancel=cancel
                         @complete="handleComplete"
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
        workflowStarted: boolean
        entryStep: string | null
        backButtonVisible: boolean
    }

    interface Methods {
        cancel: () => void
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
                entryStep: null,
                backButtonVisible: true
            }
        },
        methods: {
            handleRerun: function () {
                // can set metadata require to rerun here
                this.backButtonVisible = false
                this.entryStep = "run"
                this.workflowStarted = true
            },
            handleCreate: function () {
                this.backButtonVisible = true
                this.entryStep = "report"
                this.workflowStarted = true
            },
            handleClone: function () {
                //Can set metadata required for clone here
                this.backButtonVisible = true
                this.entryStep = "report"
                this.workflowStarted = true
            },
            cancel: function () {
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