<template>
    <div id="create-workflow-container">
        <h2 id="create-workflow-header">
            Run workflow
        </h2>
        <div class="pt-2 col-sm-6">
            <p>Either:</p>
            <button id="create-workflow" type="button"
                    class="btn btn-success" @click="create()">
                Create a blank workflow
            </button>
        </div>
        <div id="report-list" class="pt-4 col-sm-8">
            <p>Or re-use an existing workflow:</p>
            <div id="v-select">
                <v-select v-model="selectedWorkflow"
                          label="name"
                          :filter="searchWorkflows"
                          :options="workflows.slice(0, 10)"
                          placeholder="Search by name or user...">
                    <template #option="{ name, email, date }">
                        <div class="workflow-options">
                            <span>{{ name }}</span>
                            <span class="text-muted pl-3">{{ email }} | {{ getLongTimestamp(date) }}</span>
                        </div>
                    </template>
                </v-select>
            </div>
        </div>
        <div class="pt-4 col-sm-6">
            <button id="rerun" type="button"
                    class="btn btn-success"
                    :disabled="enableButtons"
                    @click="rerun()">Re-run workflow
            </button>
            <button id="clone" type="button"
                    class="btn btn-success" :disabled="enableButtons"
                    @click="clone()">Clone workflow
            </button>
        </div>
        <div class="pt-4 col-sm-6">
            <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import vSelect from 'vue-select'
    import {api} from "../../utils/api";
    import {RunWorkflowMetadata, WorkflowRunSummary} from "../../utils/types";
    import {longTimestamp, workflowRunDetailsToMetadata} from "../../utils/helpers.ts";
    import ErrorInfo from "../errorInfo.vue";

    interface Methods {
        create: () => void
        clone: () => void
        rerun: () => void
        getWorkflows: () => void
        getWorkflowDetails: () => void
        searchWorkflows: (options: [], search: string) => void
        getLongTimestamp: (date: string) => string
    }

    interface Data {
        workflows: WorkflowRunSummary[]
        error: string | null
        defaultMessage: string | null
        selectedWorkflow: string
        runWorkflowMetadata: RunWorkflowMetadata | null
    }

    const emptyWorkflowMetadata = {
        name: "",
        reports: [],
        instances: {},
        git_branch: null,
        git_commit: null,
        changelog: null
    };

    interface Computed {
        enableButtons: boolean
    }

    export default Vue.extend<Data, Methods, Computed>({
        name: "RunWorkflowCreate",
        components: {
            vSelect,
            ErrorInfo
        },
        data(): Data {
            return {
                workflows: [],
                error: "",
                defaultMessage: "",
                selectedWorkflow: null,
                runWorkflowMetadata: null
            }
        },
        computed: {
            enableButtons: function () {
                return !this.selectedWorkflow || !this.runWorkflowMetadata
            }
        },
        watch: {
            selectedWorkflow() {
                if (this.selectedWorkflow) {
                    this.getWorkflowDetails()
                }
            }
        },
        mounted() {
            this.getWorkflows()
        },
        methods: {
            create: function () {
                this.$emit("create", emptyWorkflowMetadata)
            },
            clone: function () {
                if (this.selectedWorkflow && this.runWorkflowMetadata) {
                    const clonedWorkflow = {
                        ...workflowRunDetailsToMetadata(this.runWorkflowMetadata),
                        name: "",
                        instances: {}
                    }
                    this.$emit("clone", clonedWorkflow)
                }
            },
            rerun: function () {
                if (this.selectedWorkflow && this.runWorkflowMetadata) {
                    const runnableWorkflow = workflowRunDetailsToMetadata(this.runWorkflowMetadata)
                    this.$emit("rerun", runnableWorkflow)
                }
            },
            getWorkflows: function () {
                api.get(`/workflows`)
                    .then(({data}) => {
                        this.workflows = data.data
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving previously run workflows";
                    })
            },
            getWorkflowDetails: function () {
                api.get(`/workflows/${this.selectedWorkflow.key}/`)
                    .then(({data}) => {
                        this.runWorkflowMetadata = data.data
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving workflow details";
                    })
            },
            searchWorkflows: function (options, search) {
                return this.workflows.filter(option => {
                    const {email, name} = option;
                    return ([email, name].toString()).toLowerCase().indexOf(search.toLowerCase()) > -1
                });
            },
            getLongTimestamp: function (date) {
                const dateConverter = new Date(date)
                return longTimestamp(dateConverter)
            }
        }
    })
</script>
