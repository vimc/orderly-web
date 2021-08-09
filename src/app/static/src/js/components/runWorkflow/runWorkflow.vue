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
                         @update-run-workflow-metadata="updateRunWorkflowMetadata"
                         :disable-rename="disableRename"
                         :initial-run-workflow-metadata="runWorkflowMetadata">
        </workflow-wizard>
        <div class="pt-4">
            <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import workflowWizard from "../workflowWizard/workflowWizard.vue";
    import {RunWorkflowMetadata, WorkflowMetadata, Step} from "../../utils/types"
    import runWorkflowCreate from "./runWorkflowCreate.vue";
    import { api } from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";

    interface Data {
        runWorkflowMetadata: RunWorkflowMetadata | null
        workflowStarted: boolean
        stepComponents: Step[]
        toggleFinalStepNextTo: string | null
        disableRename: boolean
        error: string | null
    }

    interface Computed {
        workflowMetadata: WorkflowMetadata | null
    }

    interface Methods {
        handleCancel: () => void
        handleRerun: (data: Event) => void
        handleCreate: (data: Event) => void
        handleClone: (data: Event) => void
        handleComplete: () => void
        updateRunWorkflowMetadata: (data: RunWorkflowMetadata) => void
    }

export default Vue.extend<Data, Methods, Computed, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            runWorkflowMetadata: null,
            workflowStarted: false,
            stepComponents: [],
            toggleFinalStepNextTo: "Run workflow",
            disableRename: false,
            error: "",
        }
    },
    computed: {
        workflowMetadata(){
            const { name, reports, changelog } = this.runWorkflowMetadata;
            return { name, reports, changelog }
        }
    },
    methods: {
        handleRerun: function (data) {
            this.runWorkflowMetadata = data
            this.stepComponents = [{name: "run", component: "runWorkflowRun"}]
            this.workflowStarted = true
            this.disableRename = true
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
            this.disableRename = false
        },
        handleComplete: function () {
            // const data = this.runWorkflowMetadata
            // const data: RunWorkflowMetadata = {
            // const data = {
            //     name: "My workflow",
            //     reports: [{"name":"minimal"},{"name":"global"}],
            //     changelog: {"message":"message1","type":"internal"}
            // };
            // console.log("runWorkflowMetadata", this.runWorkflowMetadata);
            // const { name, reports, changelog } = this.runWorkflowMetadata;
            // const data = {
            //     name,
            //     reports,
            //     changelog
            // }
            // console.log("data", this.workflowMetadata);
            api.post(`/workflow`, this.workflowMetadata)
                .then((response) => {
                    this.error = null;
                    console.log("response", response);
                    this.$emit("view-progress", response.data.data.workflow_key)
                })
                .catch((error) => {
                    // console.log("error", error);
                    this.error = error;
                    this.defaultMessage = "An error occurred while running the workflow";
                });
        },
        updateRunWorkflowMetadata: function (update) {
            this.runWorkflowMetadata = update;
        }
    },
    components: {
        workflowWizard,
        runWorkflowCreate,
        ErrorInfo
    },
    // watch: {
    //     runWorkflowMetadata(){
    //         console.log("runWorkflowMetadata", this.runWorkflowMetadata);
    //     }
    // }
    // mounted(){
    //     this.handleComplete()
    // }
})
</script>
