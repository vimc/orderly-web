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
                         :disable-rename="disableRename"
                         :initial-run-workflow-metadata="runWorkflowMetadata"
                         @cancel="handleCancel"
                         @complete="handleComplete"
                         @update-run-workflow-metadata="updateRunWorkflowMetadata">
        </workflow-wizard>
        <div v-if="createdWorkflowKey" id="view-progress-link" class="text-secondary mt-2 pl-3">
            <a href="#" @click.prevent="$emit('view-progress', createdWorkflowKey)">View workflow progress</a>
        </div>
        <div class="pt-4 col-sm-6">
            <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import workflowWizard from "../workflowWizard/workflowWizard.vue";
    import {RunWorkflowMetadata, Step} from "../../utils/types"
    import runWorkflowCreate from "./runWorkflowCreate.vue";
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import {session} from "../../utils/session";

    interface Props {
        workflowToRerun: RunWorkflowMetadata | null
    }

    interface Data {
        runWorkflowMetadata: RunWorkflowMetadata | null
        workflowStarted: boolean
        stepComponents: Step[]
        toggleFinalStepNextTo: string | null
        disableRename: boolean
        error: string | null
        createdWorkflowKey: string
    }

    interface Methods {
        handleCancel: () => void
        handleRerun: (data: Event) => void
        handleCreate: (data: Event) => void
        handleClone: (data: Event) => void
        handleComplete: () => void
        updateRunWorkflowMetadata: (data: RunWorkflowMetadata) => void
        resetSelectedWorkflowReportSource: () => void
    }

    export default Vue.extend<Data, Methods, unknown, Props>({
        name: "RunWorkflow",
        components: {
            workflowWizard,
            runWorkflowCreate,
            ErrorInfo
        },
        props: {
            workflowToRerun: null
        },
        data(): Data {
            return {
                runWorkflowMetadata: null,
                workflowStarted: false,
                stepComponents: [],
                toggleFinalStepNextTo: "Run workflow",
                disableRename: false,
                error: "",
                createdWorkflowKey: ""
            }
        },
        mounted() {
            if (this.workflowToRerun) {
                this.handleRerun(this.workflowToRerun);
            }
        },
        methods: {
            handleRerun: function (data) {
                this.runWorkflowMetadata = data
                this.stepComponents = [
                    {name: "summary", component: "runWorkflowSummary"},
                    {name: "run", component: "runWorkflowRun"}]
                this.workflowStarted = true
                this.disableRename = true
            },
            handleCreate: function (data) {
                this.runWorkflowMetadata = data
                this.stepComponents = [
                    {name: "report", component: "runWorkflowReport"},
                    {name: "summary", component: "runWorkflowSummary"},
                    {name: "run", component: "runWorkflowRun"},
                ]
                this.resetSelectedWorkflowReportSource();
                this.workflowStarted = true
            },
            handleClone: function (data) {
                this.runWorkflowMetadata = data
                this.stepComponents = [
                    {name: "report", component: "runWorkflowReport"},
                    {name: "summary", component: "runWorkflowSummary"},
                    {name: "run", component: "runWorkflowRun"},
                ]
                this.resetSelectedWorkflowReportSource();
                this.workflowStarted = true
            },
            handleCancel: function () {
                this.workflowStarted = false
                this.disableRename = false
            },
            handleComplete: function () {
                api.post(`/workflow`, this.runWorkflowMetadata)
                    .then((response) => {
                        this.error = null;
                        this.createdWorkflowKey = response.data.data.workflow_key;
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred while running the workflow";
                    });
            },
            updateRunWorkflowMetadata: function (update) {
                this.error = "";
                this.createdWorkflowKey = "";
                this.runWorkflowMetadata = update;
            },
            resetSelectedWorkflowReportSource() {
                session.setSelectedWorkflowReportSource(null);
            }
        }
    })
</script>
