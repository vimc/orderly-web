<template>
    <div class="container">
        <run-workflow-create
            v-if="!workflowStarted"
            @rerun="handleRerun"
            @create="handleCreate"
            @clone="handleClone"
        >
        </run-workflow-create>
        <workflow-wizard
            v-if="workflowStarted"
            :steps="stepComponents"
            :submit-label="toggleFinalStepNextTo"
            @cancel="handleCancel"
            @complete="handleComplete"
            :disable-rename="disableRename"
            :run-workflow-metadata="runWorkflowMetadata"
        >
        </workflow-wizard>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import workflowWizard from "../workflowWizard/workflowWizard.vue";
import { RunWorkflowMetadata, Step } from "../../utils/types";
import runWorkflowCreate from "./runWorkflowCreate.vue";
import { api } from "../../utils/api";

interface Data {
    runWorkflowMetadata: RunWorkflowMetadata | null;
    workflowStarted: boolean;
    stepComponents: Step[];
    toggleFinalStepNextTo: string | null;
    disableRename: boolean
}

interface Methods {
    handleCancel: () => void;
    handleRerun: (data: Event) => void;
    handleCreate: () => void;
    handleClone: (data: Event) => void;
    handleComplete: () => void;
}

export default Vue.extend<Data, Methods, unknown, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            runWorkflowMetadata: null,
            workflowStarted: false,
            stepComponents: [],
            toggleFinalStepNextTo: "Run workflow",
            disableRename: false
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
                { name: "report", component: "runWorkflowReport" },
                { name: "run", component: "runWorkflowRun" },
            ];
            this.workflowStarted = true;
        },
        handleClone: function (data) {
            this.runWorkflowMetadata = data;
            this.stepComponents = [
                { name: "report", component: "runWorkflowReport" },
                { name: "run", component: "runWorkflowRun" },
            ];
            this.workflowStarted = true;
        },
        handleCancel: function () {
            this.workflowStarted = false
            this.disableRename = false
        },
        handleComplete: function () {
            // const data = this.runWorkflowMetadata
            const data: RunWorkflowMetadata = {
                name: "name1",
                date: "2021-06-18T16:28:16Z",
                email: "email@email.com",
                reports: [],
                instances: {},
                git_branch: "string",
                git_commit: "string",
                key: "string",
            };
            console.log("data", data);
            api.post(`/workflow`, data)
                .then((response) => {
                    // this.error = null;
                    console.log("response", response);
                })
                .catch((error) => {
                    console.log("error", error);
                    // this.error = error;
                    // this.defaultMessage = `could not add user`;
                });
        },
    },
    components: {
        workflowWizard,
        runWorkflowCreate
    }
})
</script>
