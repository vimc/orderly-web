<template>
    <div class="container">
        <workflow-wizard>
            <step v-for="step in steps"
                  v-if="!step.hide"
                  :key="step.number"
                  :active="isActive(step.number)"
                  :number="step.number"
                  :name="step.name"
                  @jump="jump">
                <component :is="step.component"
                           :workflow-metadata="RunWorkflowMetadata"
                           @jump="jump">
                </component>
            </step>
        </workflow-wizard>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import runWorkflowCreate from "./runWorkflowCreate.vue";
import runWorkflowReport from "./runWorkflowReport.vue";
import runWorkflowSummary from "./runWorkflowSummary.vue";
import runWorkflowRun from "./runWorkflowRun.vue";
import workflowWizard from "../workflowWizard/workflowWizard.vue";
import step from "../workflowWizard/step.vue";
import {RunWorkflowMetadata} from "../../utils/types"

interface Data {
    activeStep: number
    steps: {}
    RunWorkflowMetadata: RunWorkflowMetadata | null
}

interface Methods {
    isActive: (num: number) => boolean
    jump: (action: string) => void
    nextTab: () => void
    backTab: () => void
    cancelTab: () => void
    runTab: () => void
    hideReportTab: () => void
    unHideReportTab: () => void
    handleRerun: () => void
    handleClone: () => void
    handleCreate: () => void
}

const steppers = [
    {name: "create", number: 1, hide: false, component: "runWorkflowCreate"},
    {name: "report", number: 2, hide: false, component: "runWorkflowReport"},
    {name: "summary", number: 3, hide: false, component: "runWorkflowSummary"},
    {name: "run", number: 4, hide: false, component: "runWorkflowRun"}
]

export default Vue.extend<Data, Methods, unknown, unknown>({
    name: "runWorkflow",
    data(): Data {
        return {
            activeStep: 1,
            steps: steppers,
            RunWorkflowMetadata: {placeholder: "Shared state works"}
        }
    },
    methods: {
        isActive: function (num: number) {
            return num === this.activeStep
        },
        nextTab: function () {
            if (this.activeStep < this.steps.length) {
                this.activeStep += 1
            }
        },
        backTab: function () {
            const reportPage = this.steps.find(step => step.name === "report")
            const summaryPage = this.steps.find(step => step.name === "summary")

            if (this.activeStep !== reportPage.number && !reportPage.hide) {
                this.activeStep -= 1

            } else if (this.activeStep !== summaryPage.number && reportPage.hide) {
                this.activeStep -= 1
            }
        },
        cancelTab: function () {
            this.activeStep = 1
        },
        runTab: function () {
            //Run endpoint should go here
        },
        hideReportTab: function () {
                const index = this.steps.findIndex(step => step.name === "report")
                this.steps[index].hide = true
        },
        unHideReportTab: function () {
            const index = this.steps.findIndex(step => step.name === "report")
            this.steps[index].hide = false
        },
        handleRerun: function () {
            this.hideReportTab()
            this.activeStep = 4
        },
        handleClone: function () {
            this.unHideReportTab()
            this.activeStep = 2
        },
        handleCreate: function () {
            this.unHideReportTab()
            this.activeStep = 2
        },
        jump: function (value) {
            switch (value) {
                case "next":
                    this.nextTab()
                    break;
                case "back":
                    this.backTab()
                    break;
                case "cancel":
                    this.cancelTab()
                    break;
                case "run":
                    this.runTab()
                    break;
                case "create":
                    this.handleCreate()
                    this.handleCreater
                    break
                case "rerun":
                    this.handleRerun()
                    break
                case "clone":
                    this.handleClone()
            }
        }
    },
    components: {
        runWorkflowCreate,
        runWorkflowReport,
        runWorkflowSummary,
        runWorkflowRun,
        step,
        workflowWizard
    }
})
</script>