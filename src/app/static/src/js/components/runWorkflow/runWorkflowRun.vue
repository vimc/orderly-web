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
        <div v-if="showInstances">
            <instances :instances="runMetadata.metadata.instances"
                       @selectedValues="handleInstancesValue"/>
        </div>
        <div v-if="runMetadata">
            <change-log :show-changelog="handleShowChangeLog"
                        :changelog-type-options="this.runMetadata.metadata.changelog_types"
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
    import {RunReportMetadata, RunWorkflowMetadata, WorkflowSummary} from "../../utils/types";
    import {api} from "../../utils/api";
    import ErrorInfo from "../../../js/components/errorInfo.vue";
    import ChangeLog from "../../../js/components/runReport/changeLog.vue";
    import Instances from "../../../js/components/runReport/instances.vue";

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
        runMetadata: RunReportMetadata | null;
        changeLogTypeValue: string;
        changeLogMessageValue: string;
        workflows: WorkflowSummary[];
        workflowNameError: string,
        switchV2FeatureComponentOn: boolean
    }

    interface Methods {
        getRunMetadata: () => void
        handleValidation: () => void
        getWorkflows: () => void
        handleChangeLogTypeValue: (changelogType: string) => void
        handleChangeLogMessageValue: (changelogMessage: string) => void
        handleInstancesValue: (instances: Object) => void
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
                switchV2FeatureComponentOn: false
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
            handleInstancesValue: function (instances) {
                this.selectedInstances = instances
            },
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

                /**
                 * This code is dependent on Emma's report branch. We are likely to merge that
                 * before before we can test that the emit works with the workflow wizard
                 *
                 */
                if(this.workflowName) {
                    this.$emit("update", {name: this.workflowName})
                }
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
        components: {
            ErrorInfo,
            ChangeLog,
            Instances
        }
    })
</script>