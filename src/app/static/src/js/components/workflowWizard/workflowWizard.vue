<template>
    <div id="workflow-wizard" class="container">
        <step v-for="step in steps"
              :key="getCurrentIndex(step.name)"
              :active="isActive(step.name)"
              :hasVisibility="handleVisibility(step.name)"
              :valid="validStep"
              @back="back(step.name)"
              @next="next(step.name)"
              @cancel="confirmCancel">
            <component :is="step.component"
                       @valid="handleStepValidity"
                       :workflow-metadata="runWorkflowMetadata">
            </component>
        </step>
        <cancel-dialog @cancel="cancel" @abortCancel="abortCancel" :show-modal="showModal"/>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import step from "../workflowWizard/step.vue"
    import {RunWorkflowMetadata, Steps} from "../../utils/types"
    import runWorkflowReport from "../runWorkflow/runWorkflowReport.vue"
    import runWorkflowRun from "../runWorkflow/runWorkflowRun.vue"
    import cancelDialog from "../runWorkflow/cancelDialog.vue"

    interface Data {
        activeStep: number
        validStep: boolean
        showModal: boolean
    }

    interface Methods {
        isActive: (name: string) => boolean
        next: (name: string) => void
        back: (name: string) => void
        cancel: () => void
        confirmCancel: () => void
        abortCancel: () => void
        handleVisibility: (name: string) => {}
        getCurrentIndex: (name: string) => number
        handleStepValidity: (valid: Event) => void
    }

    interface Props {
        runWorkflowMetadata: RunWorkflowMetadata | null
        backButtonVisible: boolean
        steps: Steps[]
    }

    export default Vue.extend<Data, Methods, unknown, Props>({
        name: "workflowWizard",
        props: {
            runWorkflowMetadata: null,
            backButtonVisible: {
                type: Boolean,
                required: false
            },
            steps: {
                type: [],
                required: true
            }
        },
        data(): Data {
            return {
                activeStep: 0,
                validStep: false,
                showModal: false
            }
        },
        methods: {
            handleVisibility(name) {
                const currentIndex = this.getCurrentIndex(name)
                if (currentIndex === this.steps.length - 1) {
                    return {
                        cancel: true,
                        next: false,
                        back: this.backButtonVisible
                    }
                }
                if (currentIndex === 0) {
                    return {
                        cancel: true,
                        next: true,
                        back: false
                    }
                }
                return {
                    cancel: true,
                    next: true,
                    back: true
                }
            },
            isActive: function (name: string) {
                return this.getCurrentIndex(name) === this.activeStep
            },
            next: function (name) {
                if (this.validStep) {
                    if (this.steps.length - 1 === this.getCurrentIndex(name)) {
                        this.$emit("complete", true)
                    } else {
                        this.activeStep = this.steps.findIndex(step => step.name === name) + 1
                    }
                }
            },
            back: function (name) {
                    if (this.getCurrentIndex(name) !== 0) {
                        this.activeStep = this.steps.findIndex(step => step.name === name) - 1
                    }
            },
            cancel: function () {
                this.$emit("cancel")
            },
            confirmCancel: function() {
                this.showModal = true;
            },
            abortCancel: function() {
                this.showModal = false;
            },
            getCurrentIndex: function (name) {
                return this.steps.findIndex(step => step.name === name)
            },
            handleStepValidity: function (valid) {
                this.validStep = valid
            }
        },
        components: {
            runWorkflowReport,
            runWorkflowRun,
            step,
            cancelDialog
        }
    })
</script>