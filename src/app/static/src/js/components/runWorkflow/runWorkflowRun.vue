<template>
    <div>
        <h2 id="run-header">Run workflow</h2>
        <div id="workflow-name-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Name</label>
            <div class="col-sm-4">
                <input type="text"
                       :disabled="isRerun"
                       @input="handleValidation"
                       v-model="workflowName"
                       class="form-control"
                       id="run-workflow-name"
                       placeholder="Impact estimates">
            </div>
        </div>
        <template v-if="showInstances">
            <div v-for="(options, name) in runMetadata.metadata.instances" v-if="options.length > 1"
                 id="workflow-source-div" class="form-group row">
                <label :for="name" class="col-sm-4 col-form-label text-left">Database "{{ name }}"</label>
                <div class="col-sm-4">
                    <select class="form-control" :id="name"
                            @input="handleValidation"
                            v-model="selectedInstances[name]">
                        <option v-for="option in options" :value="option">
                            {{ option }}
                        </option>
                    </select>
                </div>
            </div>
        </template>
        <div v-if="showChangeMessage">
        <div id="workflow-changelog-div" class="form-group row">
            <label id="workflow-changelog-label" class="col-sm-4 col-form-label text-left">Changelog message</label>
            <div class="col-sm-4">
                <input type="text"
                       @input="handleValidation"
                       v-model="changeLogMessage"
                       class="form-control"
                       id="run-workflow-changelog">
            </div>
        </div>
        <div id="workflow-changelog-type-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Changelog type</label>
            <div class="col-sm-4">
                <select class="form-control" id="run-workflow-changelog-type"
                        @input="handleValidation"
                        v-model="changeLogTypeValue">
                    <option v-for="option in runMetadata.metadata.changelog_types" :value="option">
                        {{ option }}
                    </option>
                </select>
            </div>
        </div>
        </div>
        <div v-if="false" id="workflow-tags-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Report version tags</label>
            <div class="col-sm-4">
                <input type="text" @input="handleValidation" class="form-control" id="run-workflow-report-version-tags" placeholder="interim estimates">
            </div>
        </div>
        <div v-if="false" id="workflow-completion-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Only commit reports on workflow completion</label>
            <div id="run-workflow-ticked" class="col-sm-4">
                <p>ticked</p>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import {RunMetadata, RunWorkflowMetadata} from "../../utils/types";
import {api} from "../../utils/api";

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
    isRerun: boolean
}

interface Computed {
    validateStep: void,
    showInstances: boolean,
    showChangeMessage: string
}

interface Data {
    selectedInstances: {};
    workflowName: string;
    runMetadata: RunMetadata | null;
    changeLogTypeValue: string;
    changeLogMessage: string;
}

interface Methods {
    getRunMetadata: () => void
    handleValidation: () => void
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
            changeLogMessage: "",
            error: "",
            defaultMessage: ""
        }
    },
    computed: {
        showInstances() {
            return !!this.runMetadata && this.runMetadata.metadata.instances_supported;
        },
        showChangeMessage: function () {
            return this.runMetadata && this.runMetadata.metadata.changelog_types;
        },
        validateStep: function () {
            /**
             * Valid step should be set to true or false once validation is complete
             */
            this.$emit("valid", this.handleValidation);
        }
    },
    methods: {
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
        handleValidation: function () {
            const valid = !!this.workflowName && !!this.changeLogMessage && !!this.changeLogTypeValue
            this.$emit("valid", valid);
        }
    },
    mounted() {
        this.getRunMetadata();
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
    }
})
</script>