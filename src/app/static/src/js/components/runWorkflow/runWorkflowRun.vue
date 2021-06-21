<template>
    <div id="run-workflow-id">
        <h2 id="run-header">Run workflow</h2>
        <div id="workflow-name-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Name</label>
            <div class="col-sm-4">
                <input type="text"
                       :readonly="isRerun"
                       @input="handleValidation"
                       v-model="workflowName"
                       class="form-control"
                       id="run-workflow-name">
                <small class="text-danger">{{ workflowNameError }}</small>
            </div>
        </div>
        <template v-if="showInstances">
            <div v-for="(options, name) in runMetadata.metadata.instances" v-if="options.length > 1"
                 id="workflow-instances-div" class="form-group row">
                <label :for="name" class="col-sm-4 col-form-label text-left">Database "{{ name }}"</label>
                <div class="col-sm-4">
                    <select class="form-control" :id="name"
                            v-model="selectedInstances[name]">
                        <option v-for="option in options" :value="option">
                            {{ option }}
                        </option>
                    </select>
                </div>
            </div>
        </template>
        <div v-if="runMetadata">
            <change-log :show-changelog="handleShowChangeLog"
                        :changelog-type-options="this.runMetadata.metadata.changelog_types"
                        :changelog-style="changelogStyle"
                        @changelogMessage="handleChangeLogMessageValue"
                        @changelogType="handleChangeLogTypeValue">
            </change-log>
        </div>
        <div v-if="switchV2FeatureComponentOn" id="workflow-tags-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Report version tags</label>
            <div class="col-sm-4">
                <input type="text" class="form-control" id="run-workflow-report-version-tags">
            </div>
        </div>
        <div v-if="switchV2FeatureComponentOn" id="workflow-completion-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Only commit reports on workflow completion</label>
            <div id="run-workflow-ticked" class="col-sm-4">
                <p>ticked</p>
            </div>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {ChangelogStyle, RunMetadata, RunWorkflowMetadata, WorkflowSummary} from "../../utils/types";
    import {api} from "../../utils/api";
    import ErrorInfo from "../../../js/components/errorInfo.vue";
    import ChangeLog from "../../../js/components/runReport/changeLog.vue";

    interface Props {
        workflowMetadata: RunWorkflowMetadata | null
        isRerun: boolean
    }

    interface Computed {
        showInstances: boolean,
        handleShowChangeLog: void
    }

    interface Data {
        selectedInstances: {};
        workflowName: string;
        runMetadata: RunMetadata | null;
        changeLogTypeValue: string;
        changeLogMessageValue: string;
        workflows: WorkflowSummary[];
        workflowNameError: string,
        switchV2FeatureComponentOn: boolean,
        changelogStyle: ChangelogStyle;
    }

    interface Methods {
        getRunMetadata: () => void
        handleValidation: () => void
        getWorkflows: () => void
        handleChangeLogTypeValue: (changelogType: string) => void
        handleChangeLogMessageValue: (changelogMessage: string) => void
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runWorkflowRun",
        props: {
            workflowMetadata: null,
            isRerun: {
                required: false,
                type: Boolean
            }
        },
        data() {
            return {
                selectedInstances: {},
                workflowName: "",
                runMetadata: null,
                changeLogTypeValue: "",
                changeLogMessageValue: "",
                error: "",
                defaultMessage: "",
                workflows: [],
                workflowNameError: "",
                /**
                 * switchV2FeatureComponentOn variable should be removed when we are ready to implement v2
                 * This variable was added because I didnt want to remove the code
                 * created. This blocked section is meant to be implemented in v2.
                 */
                switchV2FeatureComponentOn: false,
                changelogStyle: {
                    label: {size: 4, justify: "text-left"},
                    control: {size: 4}
                }
            }
        },
        computed: {
            showInstances() {
                return !!this.runMetadata && this.runMetadata.metadata.instances_supported;
            },
            handleShowChangeLog: function () {
                return this.runMetadata && !!this.runMetadata.metadata.changelog_types;
            }
        },
        methods: {
            handleChangeLogTypeValue: function (changelogType) {
                this.changeLogTypeValue = changelogType
            },
            handleChangeLogMessageValue: function (changelogMessage) {
                this.changeLogMessageValue = changelogMessage
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
                    })
                    .catch((error) => {
                        this.error = error
                        this.defaultMessage = "An error occurred while retrieving previously run workflows";
                    })
            },
            handleValidation: function () {
                let valid = !!this.workflowName;
                this.workflowNameError = "";
                if (this.workflows.find(workflow =>
                    workflow.name.toLowerCase() === this.workflowName.toLowerCase())) {
                    valid = false;
                    this.workflowNameError = "Workflow name already exists, please rename your workflow."
                }
                this.$emit("valid", valid);
                this.$emit("update", {name: this.workflowName})
            }
        },
        mounted() {
            this.getRunMetadata();

            if (!this.isRerun) {
                this.getWorkflows();
            }
            if (this.isRerun) {
                this.workflowName = this.workflowMetadata.name
                this.$emit("valid", true);
            }
        },
        watch: {
            runMetadata() {
                if (this.runMetadata && this.runMetadata.metadata.changelog_types) {
                    this.changeLogTypeValue = this.runMetadata.metadata.changelog_types[0];
                }

                if (this.runMetadata && this.runMetadata.metadata.instances_supported) {
                    const instances = this.runMetadata.metadata.instances;
                    for (const key in instances) {
                        if (instances[key].length > 0) {
                            this.$set(this.selectedInstances, key, instances[key][0]);
                        }
                    }
                }
            }
        },
        components: {
            ErrorInfo,
            ChangeLog
        }
    })
</script>