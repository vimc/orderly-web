<template>
    <div id="workflow-wizard" class="container">
            <step v-for="step in steps"
                  :key="getCurrentIndex(step.name)"
                  :active="isActive(step.name)"
                  :hasVisibility="handleVisibility(step.name)"
                  :valid="isValid"
                  @back="back(step.name)"
                  @next="next(step.name)"
                  @cancel="cancel">
                <component :is="step.component"
                           @valid="validate"
                           :workflow-metadata="runWorkflowMetadata">
                </component>
            </step>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import step from "../workflowWizard/step.vue";
    import {RunWorkflowMetadata} from "../../utils/types"
    import runWorkflowReport from "../runWorkflow/runWorkflowReport.vue";
    import runWorkflowRun from "../runWorkflow/runWorkflowRun.vue";

    interface Data {
        activeStep: number
        steps: {},
        isValid: boolean
    }

    interface Methods {
        isActive: (name: string) => boolean
        next: (name: string) => void
        back: (name: string) => void
        cancel: () => void
        handleVisibility: (name: string) => {}
        getCurrentIndex: (name: string) => number
        validate: (valid: Event) => void
    }

    interface Props {
        runWorkflowMetadata: RunWorkflowMetadata | null
        entryStep: string | null
    }

    const steps = [
        {name: "report", component: "runWorkflowReport"},
        {name: "run", component: "runWorkflowRun"},
    ]

    interface Computed {
        isValid: boolean
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "workflowWizard",
        props: {
            runWorkflowMetadata: null,
            entryStep: null
        },
        data(): Data {
            return {
                activeStep: steps.findIndex(step => step.name === this.entryStep),
                steps: steps,
                isValid: false
            }
        },
        methods: {
            handleVisibility(name) {
                const number = this.getCurrentIndex(name)

                /**
                 * hides back button and next button changes
                 * to run workflow when on final step
                 */
                if (number + 1 === this.steps.length) {
                    return {cancel: true, next: false, back: false}
                }

                /**
                 * Back button gets hidden when on first step
                 */
                if (number === 0) {
                    return {cancel: true, next: true, back: false}
                }
                return {cancel: true, next: true, back: true}
            },
            isActive: function (name: string) {
                return this.getCurrentIndex(name) === this.activeStep
            },
            next: function (name) {
                if (this.isValid) {
                    if (this.steps.length === this.getCurrentIndex(name) + 1) {
                        this.$emit("complete", true)
                    } else {
                        this.activeStep = steps.findIndex(step => step.name === name) + 1
                    }
                }
            },
            back: function (name) {
                if (this.isValid) {
                    if (this.getCurrentIndex(name) !== 0) {
                        this.activeStep = steps.findIndex(step => step.name === name) - 1
                    }
                }
            },
            cancel: function () {
                //Todo: Change the confirm dialog implementation to something more appealing
                if(confirm("Are you sure you want to cancel?")) {
                    this.$emit("cancel")
                }
            },
            getCurrentIndex: function (name) {
                return steps.findIndex(step => step.name === name)
            },
            validate: function (valid) {
                this.isValid = !!valid
            }
        },
        components: {
            runWorkflowReport,
            runWorkflowRun,
            step
        }
    })
</script>