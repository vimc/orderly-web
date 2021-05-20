<template>
    <div>
        <h2 id="create-workflow-header">Run workflow</h2>
        <div class="pt-2 col-sm-6">
            <p>Either:</p>
            <button id="create-workflow" @click="create()"
                    type="button" class="btn btn-success">
                Create a blank workflow
            </button>
        </div>
        <div id="report-list" class="pt-4 col-sm-6">
            <p>Or re-use an existing workflow:</p>
            <div>
                <v-select label="name"
                          :filter="searchWorkflows"
                          :options="workflows.slice(0, 10)"
                          v-model="selectedWorkflow"
                          placeholder="Search by name or user...">
                    <template #option="{ name, email, date }">
                        <div>{{ name }}
                            <span style="opacity: 0.5; float:right;">
                            {{ handleUser(email) }} | {{ handleDate(date) }}</span>
                        </div>
                    </template>
                </v-select>
            </div>
        </div>
        <div class="pt-4 col-sm-6">
            <button id="rerun" @click="rerun()"
                    type="button"
                    class="btn btn-success"
                    :disabled="!selectedWorkflow && !error">Re-run workflow
            </button>
            <button id="clone" @click="clone()"
                    type="button" class="btn btn-success"
                    :disabled="!selectedWorkflow && !error">Clone workflow
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
    import {RunWorkflowMetadata} from "../../utils/types";
    import "vue-select/dist/vue-select.css";
    import {longTimestamp} from "../../utils/helpers";
    import ErrorInfo from "../errorInfo.vue";

    interface Methods {
        create: () => void
        clone: () => void
        rerun: () => void
        getWorkflows: () => void
        getCloneableWorkflow: () => void
        searchWorkflows: (options: [], search: string) => void
        handleDate: (date: string) => string
        handleUser: (user: string) => string
    }

    interface Data {
        workflows: []
        error: string | null
        defaultMessage: string | null
        selectedWorkflow: string
        runWorkflowMetadata: RunWorkflowMetadata | null
    }

    const cloneableWorkflowMetadata = {
        name: null,
        date: null,
        email: null,
        reports: {},
        instances: {},
        git_branch: null,
        git_commit: null,
        key: null
    }

    export default Vue.extend<Data, Methods, unknown, unknown>({
        name: "runWorkflowCreate",
        data(): Data {
            return {
                workflows: [],
                error: "",
                defaultMessage: "",
                selectedWorkflow: null,
                runWorkflowMetadata: null
            }
        },
        methods: {
            create: function () {
                this.$emit("create")
            },
            clone: function () {
                if (this.selectedWorkflow) {
                    const {reports, git_branch, git_commit} = this.runWorkflowMetadata
                    const clonedWorkflow = {...cloneableWorkflowMetadata, ...{reports, git_branch, git_commit}}
                    this.$emit("clone", clonedWorkflow)
                }
            },
            rerun: function () {
                if (this.selectedWorkflow) {
                    this.$emit("rerun", this.runWorkflowMetadata)
                }
            },
            getWorkflows: function () {
                api.get(`/workflows`)
                    .then(({data}) => {
                        this.workflows = data.data
                    })
                    .catch(({error}) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving previously run workflows";
                    })
            },
            getCloneableWorkflow: function () {
                api.get(`/workflows/${this.selectedWorkflow.key}/`)
                    .then(({data}) => {
                        this.runWorkflowMetadata = data.data
                    })
                    .catch(({error}) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving workflow details";
                    })
            },
            searchWorkflows: function (options, search) {
                return this.workflows.filter(option => {
                    const {email, name} = option;
                    return ([email, name].toString() || '').toLowerCase().indexOf(search.toLowerCase()) > -1
                });
            },
            handleDate: function (date) {
                const dateConverter = new Date(date)
                return longTimestamp(dateConverter)
            },
            handleUser: function (user) {
                return user.split("@")[0]
            }
        },
        mounted() {
            this.getWorkflows()
        },
        watch: {
            selectedWorkflow() {
                if (this.selectedWorkflow) {
                    this.getCloneableWorkflow()
                }
            }
        },
        components: {
            vSelect,
            ErrorInfo
        }
    })
</script>