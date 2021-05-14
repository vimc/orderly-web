<template>
    <div id="workflow-wizard" class="container">
        <step v-for="step in steps"
              :key="getCurrentIndex(step.name)"
              :active="isActive(step.name)"
              :hasVisibility="handleVisibility(step.name)"
              :enabled="enabledButtons"
              @back="back(step.name)"
              @next="next(step.name)"
              @cancel="confirmCancel">
            <component :is="step.component"
                       @enabled="handleEnabledButtons"
                       :workflow-metadata="runWorkflowMetadata">
            </component>
        </step>
        <cancel-dialog @cancel="cancel" @abortCancel="abortCancel" :show-modal="showModal"/>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import step from "../workflowWizard/step.vue"
    import {RunWorkflowMetadata} from "../../utils/types"
    import runWorkflowReport from "../runWorkflow/runWorkflowReport.vue"
    import runWorkflowRun from "../runWorkflow/runWorkflowRun.vue"
    import cancelDialog from "../runWorkflow/cancelDialog.vue"

    interface Data {
        activeStep: number
        steps: {},
        enabledButtons: {}
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
        handleEnabledButtons: (enabled: Event) => void
    }

    interface Props {
        runWorkflowMetadata: RunWorkflowMetadata | null
        entryStep: string | null
        backButtonVisible: boolean
    }

    const steps = [
        {name: "report", component: "runWorkflowReport"},
        {name: "run", component: "runWorkflowRun"},
    ]

    export default Vue.extend<Data, Methods, unknown, Props>({
        name: "workflowWizard",
        props: {
            runWorkflowMetadata: null,
            entryStep: null,
            backButtonVisible: {
                type: Boolean,
                required: false
            }
        },
        data(): Data {
            return {
                activeStep: steps.findIndex(step => step.name === this.entryStep),
                steps: steps,
                enabledButtons: {back: false, next: false},
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
                if (this.enabledButtons.next) {
                    if (this.steps.length === this.getCurrentIndex(name) + 1) {
                        this.$emit("complete", true)
                    } else {
                        this.activeStep = steps.findIndex(step => step.name === name) + 1
                    }
                }
            },
            back: function (name) {
                if (this.enabledButtons.back) {
                    if (this.getCurrentIndex(name) !== 0) {
                        this.activeStep = steps.findIndex(step => step.name === name) - 1
                    }
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
                return steps.findIndex(step => step.name === name)
            },
            handleEnabledButtons: function (enabled) {
                this.enabledButtons = enabled
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