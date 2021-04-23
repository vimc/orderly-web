<template>
    <div class="container">
            <step v-for="step in steps"
                  v-if="!step.hide"
                  :key="step.number"
                  :active="isActive(step.number)"
                  :hasVisibility="hasVisibility(step.number)"
                  :name="step.name"
                  @jump="jump">
                <component :is="step.component"
                           :workflow-metadata="runWorkflowMetadata"
                           @jump="jump">
                </component>
            </step>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import step from "../workflowWizard/step.vue";
import {RunWorkflowMetadata} from "../../utils/types"
import runWorkflowCreate from "../runWorkflow/runWorkflowCreate.vue";
import runWorkflowSummary from "../runWorkflow/runWorkflowSummary.vue";
import runWorkflowReport from "../runWorkflow/runWorkflowReport.vue";
import runWorkflowRun from "../runWorkflow/runWorkflowRun.vue";

interface Data {
    activeStep: number
    steps: {}
    initiatedRerun: boolean
}

interface Methods {
    isActive: (num: number) => boolean
    jump: (action: string) => void
    next: () => void
    back: () => void
    cancel: () => void
    run: () => void
    hideReportStep: () => void
    unHideReportStep: () => void
    handleRerun: () => void
    handleClone: () => void
    handleCreate: () => void
    hasVisibility: (num: number) => {}
}

interface Props {
    runWorkflowMetadata: RunWorkflowMetadata | null
}

const steps = [
    {name: "create", number: 1, hide: false, component: "runWorkflowCreate"},
    {name: "report", number: 2, hide: false, component: "runWorkflowReport"},
    {name: "summary", number: 3, hide: true, component: "runWorkflowSummary"},
    {name: "run", number: 4, hide: false, component: "runWorkflowRun"}
]

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "workflowWizard",
    props: {
        runWorkflowMetadata: null
    },
    data(): Data {
        return {
            activeStep: 1,
            steps: steps,
            initiatedRerun: false
        }
    },
    methods: {
        hasVisibility(number) {
            const run = number === 4
            const cancel = number !== 1
            const next = number !== 4
            const back = number > 2

            return {run, cancel, next, back, rerun: this.initiatedRerun}
        },
        isActive: function (num: number) {
            return num === this.activeStep
        },
        next: function () {
            for (let i = this.activeStep; i < this.steps.length; i++) {
                if (!this.steps[i].hide) {
                    this.activeStep = this.steps[i].number
                    break
                }
            }
        },
        back: function () {
            for (let i = 1; i < this.activeStep; i--) {
                if (!this.steps[i].hide) {
                    this.activeStep = this.steps[i].number
                    break
                }
            }
        },
        cancel: function () {
            this.activeStep = 1
        },
        run: function () {
            //This should be emitted to runWorkflow
        },
        hideReportStep: function () {
            const index = this.steps.findIndex(step => step.name === "report")
            this.steps[index].hide = true
        },
        unHideReportStep: function () {
            const index = this.steps.findIndex(step => step.name === "report")
            this.steps[index].hide = false
        },
        handleRerun: function () {
            this.hideReportStep()
            this.initiatedRerun = true
            this.activeStep = 4
        },
        handleClone: function () {
            this.unHideReportStep()
            this.initiatedRerun = false
            this.activeStep = 2
        },
        handleCreate: function () {
            this.unHideReportStep()
            this.initiatedRerun = false
            this.activeStep = 2
        },
        jump: function (value) {
            switch (value) {
                case "next":
                    this.next()
                    break;
                case "back":
                    this.back()
                    break;
                case "cancel":
                    this.cancel()
                    break;
                case "run":
                    this.run()
                    break;
                case "create":
                    this.handleCreate()
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
        step
    }
})
</script>