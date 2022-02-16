<template>
    <div id="run-workflow-id">
        <h2 id="run-header">Run workflow</h2>
        <div id="workflow-name-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Name</label>
            <div class="col-sm-4">
                <input type="text"
                       :readonly="disableRename"
                       :value="workflowMetadata.name"
                       @input="handleWorkflowName"
                       class="form-control"
                       id="run-workflow-name">
                <small class="text-danger">{{ workflowNameError }}</small>
            </div>
        </div>
        <div v-if="showInstances">
            <instances :instances="runMetadata.metadata.instances"
                       :initial-selected-instances="workflowMetadata.instances"
                       :custom-style="childCustomStyle"
                       @selectedValues="handleInstancesValue"/>
        </div>
        <div v-if="showChangelog">
            <change-log
                        :changelog-type-options="this.runMetadata.metadata.changelog_types"
                        :custom-style="childCustomStyle"
                        :initial-message="initialChangelogMessage"
                        :initial-type="initialChangelogType"
                        @changelog="handleChangelog">
            </change-log>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {ChildCustomStyle, RunReportMetadata, RunWorkflowMetadata, WorkflowRunSummary} from "../../utils/types";
    import {api} from "../../utils/api";
    import ErrorInfo from "../../../js/components/errorInfo.vue";
    import ChangeLog from "../../../js/components/runReport/changeLog.vue";
    import Instances from "../../../js/components/runReport/instances.vue";

    interface Props {
        workflowMetadata: RunWorkflowMetadata | null
        disableRename: boolean
    }

    interface Computed {
        showInstances: boolean,
        showChangelog: void,
        initialChangelogMessage: string | null,
        initialChangelogType: string | null
    }

    interface Data {
        runMetadata: RunReportMetadata | null,
        workflows: WorkflowRunSummary[],
        workflowNameError: string,
        childCustomStyle: ChildCustomStyle
    }

    interface Methods {
        getRunMetadata: () => void
        handleWorkflowName: (event: Event) => void
        getWorkflows: () => void
        handleChangelog: (changelog: object) => void,
        handleInstancesValue: (instances: object) => void
        validateWorkflowName: (workflowName: string) => void
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runWorkflowRun",
        props: {
            workflowMetadata: null,
            disableRename: {
                required: false,
                type: Boolean
            }
        },
        data() {
            return {
                runMetadata: null,
                error: "",
                defaultMessage: "",
                workflows: [],
                workflowNameError: "",
                childCustomStyle: {label: "col-sm-4 text-left", control: "col-sm-4"}
            }
        },
        computed: {
            showInstances() {
                return !!this.runMetadata && this.runMetadata.metadata.instances_supported;
            },
            showChangelog: function () {
                return this.runMetadata && !!this.runMetadata.metadata.changelog_types;
            },
            initialChangelogMessage() {
                return this.workflowMetadata.changelog?.message
            },
            initialChangelogType() {
                return this.workflowMetadata.changelog?.type
            }
        },
        methods: {
            handleInstancesValue: function (instances) {
                this.$emit("update", {instances});
            },
            handleChangelog: function (changelog) {
                this.$emit("update", {changelog});
            },
            getRunMetadata: function () {
                api.get('/report/run-metadata')
                    .then(({data}) => {
                        this.runMetadata = data.data
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred while retrieving data";
                    });
            },
            getWorkflows: function () {
                api.get(`/workflows`)
                    .then(({data}) => {
                        this.workflows = data.data
                        this.error = "";
                        this.defaultMessage = "";
                        this.validateWorkflowName(this.workflowMetadata.name);
                    })
                    .catch((error) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving previously run workflows";
                    })
            },
            handleWorkflowName: function (event: Event) {
                const workflowName = (event.target as HTMLInputElement).value;
                this.validateWorkflowName(workflowName);
                this.$emit("update", {name: workflowName});
            },
            validateWorkflowName: function(workflowName: string) {
                let valid = !!workflowName;
                this.workflowNameError = "";
                if (this.workflows.find(workflow =>
                    workflow.name.toLowerCase() === workflowName.toLowerCase())) {
                    valid = false;
                    this.workflowNameError = "Workflow name already exists, please rename your workflow."
                }
                this.$emit("valid", valid);
            }
        },
        mounted() {
            this.getRunMetadata();
            if (this.disableRename) {
                this.$emit("valid", true);
            } else {
                this.getWorkflows();
            }
        },
        components: {
            ErrorInfo,
            ChangeLog,
            Instances
        }
    })
</script>
