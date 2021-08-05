<template>
    <div id="workflow-wizard" class="container">
        <step v-for="step in steps"
              :key="getCurrentIndex(step.name)"
              :submit-label="submitLabel"
              :active="isActive(step.name)"
              :button-options="handleButtonOptions(step.name)"
              :valid="validStep"
              @back="back(step.name)"
              @next="next(step.name)"
              @cancel="confirmCancel">
            <component :is="step.component"
                       :workflow-metadata="runWorkflowMetadata"
                       @valid="handleStepValidity"
                       :disable-rename ="disableRename"
                       @update="updateMetadata">
            </component>
        </step>
        <div id="run-workflow-status" v-if="runningStatus"
             class="row justify-content-end col-sm-8 text-secondary mt-2">
            {{ runningStatus }}
            <a @click.prevent="$emit('changeTab')" href="#">View log</a>
        </div>
        <cancel-dialog @cancel="cancel" @abortCancel="abortCancel" :show-modal="showModal"/>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import step from "../workflowWizard/step.vue"
    import {RunWorkflowMetadata, Step} from "../../utils/types"
    import runWorkflowReport from "../runWorkflow/runWorkflowReport.vue"
    import runWorkflowRun from "../runWorkflow/runWorkflowRun.vue"
    import cancelDialog from "../runWorkflow/cancelDialog.vue"

    interface Data {
        activeStep: number
        validStep: boolean
        showModal: boolean
        runWorkflowMetadata: RunWorkflowMetadata | null
        runningStatus: string
    }

    interface Methods {
        isActive: (name: string) => boolean
        next: (name: string) => void
        back: (name: string) => void
        cancel: () => void
        confirmCancel: () => void
        abortCancel: () => void
        handleButtonOptions: (name: string) => {}
        getCurrentIndex: (name: string) => number
        handleStepValidity: (valid: Event) => void
        updateMetadata: (metadata: Partial<RunWorkflowMetadata>) => void
    }

    interface Props {
        initialRunWorkflowMetadata: RunWorkflowMetadata
        steps: Step[]
        submitLabel : string | null
        disableRename: boolean
    }

    export default Vue.extend<Data, Methods, unknown, Props>({
        name: "workflowWizard",
        props: {
            initialRunWorkflowMetadata: {
                type: Object,
                required: true
            },
            steps: {
                type: Array,
                required: true
            },
            submitLabel: {
                type: String,
                required: false
            },
            disableRename: {
                type: Boolean,
                required: false
            }
        },
        data(): Data {
            return {
                activeStep: 0,
                validStep: false,
                showModal: false,
                runWorkflowMetadata: null,
                runningStatus: ""
            }
        },
        methods: {
            handleButtonOptions(name) {
                const currentIndex = this.getCurrentIndex(name)
                if (currentIndex === this.steps.length - 1) {
                    return {
                        hasCustomSubmitLabel: true,
                        back: this.steps.length !== 1
                    }
                }
                if (currentIndex === 0) {
                    return {
                        back: false
                    }
                }
                return {
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
                        this.runningStatus = "Run started"
                    } else {
                        this.activeStep = this.steps.findIndex(step => step.name === name) + 1
                        this.validStep = false
                    }
                }
            },
            back: function (name) {
                    if (this.getCurrentIndex(name) !== 0) {
                        this.activeStep = this.steps.findIndex(step => step.name === name) - 1
                        this.runningStatus = ""
                    }
            },
            cancel: function () {
                this.$emit("cancel")
                this.validStep = false
                this.runningStatus = ""
            },
            confirmCancel: function() {
                this.showModal = true
            },
            abortCancel: function() {
                this.showModal = false
            },
            getCurrentIndex: function (name) {
                return this.steps.findIndex(step => step.name === name)
            },
            handleStepValidity: function (valid) {
                this.validStep = valid
            },
            updateMetadata: function (metadata: Partial<RunWorkflowMetadata>) {
                this.runWorkflowMetadata = {
                    ...this.runWorkflowMetadata,
                    ...metadata
                };
            }
        },
        created() {
            this.runWorkflowMetadata = {
                ...this.initialRunWorkflowMetadata
            };
        },
        components: {
            runWorkflowReport,
            runWorkflowRun,
            step,
            cancelDialog
        }
    })
</script>
