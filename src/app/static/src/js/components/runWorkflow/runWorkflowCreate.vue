<template>
    <div>
        <h2 id="create-workflow-header">Run workflow</h2>
        <div class="pt-2 col-md-6">
            <p>Either:</p>
            <button id="create-workflow" @click="create()"
                    type="button" class="btn btn-success">
                Create a blank workflow
            </button>
        </div>
        <div id="report-list" class="pt-4 col-md-8">
            <p>Or re-use an existing workflow:</p>
            <div>
                <v-select label="name"
                          :options="workflows" v-model="selectedWorkflow"
                          :reduce="workflows => workflows.key"
                          placeholder="Search by name...">
                    <template #option="{ name, email, date }">
                        <span>{{ name }}<span class="text-secondary pl-4">
                            {{ email }} | {{ date }}</span>
                        </span>
                    </template>
                </v-select>
            </div>
        </div>
        <div class="pt-4 col-md-6">
            <button id="rerun" @click="rerun()"
                    type="button"
                    class="btn btn-success"
                    :disabled="!selectedWorkflow">Re-run workflow
            </button>
            <button id="clone" @click="clone()"
                    type="button" class="btn btn-success"
                    :disabled="!selectedWorkflow">Clone workflow
            </button>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import vSelect from 'vue-select'
import {api} from "../../utils/api";
import {RunWorkflowMetadata} from "../../utils/types";
import "vue-select/dist/vue-select.css";

interface Methods {
    create: () => void
    clone: () => void
    rerun: () => void
    getWorkflows: () => void
    getCloneableWorkflow: () => void
}

interface Data {
    workflows: []
    error: ""
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
            api.get(`/workflows/runnable`)
                .then(({data}) => {
                    this.workflows = data.data
                })
                .catch(({error}) => {
                    this.error = error
                })
        },
        getCloneableWorkflow: function () {
            api.get(`/workflows/${this.selectedWorkflow}/`)
                .then(({data}) => {
                    this.runWorkflowMetadata = data.data
                })
                .catch(({error}) => {
                    this.error = error
                })
        }
    },
    components: {
        vSelect
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
    }
})
</script>