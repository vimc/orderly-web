<template>
    <div>
        <h2>Run a report</h2>
        <form class="mt-3">
            <git-update-reports
                :report-metadata="metadata"
                :initial-branches="initialGitBranches"
                @branchSelected="branchSelected"
                @commitSelected="commitSelected"
                @reportsUpdate="updateReports"
            ></git-update-reports>
            <div v-if="showReports" id="report-form-group" class="form-group row">
                <label for="report" class="col-sm-2 col-form-label text-right">Report</label>
                <div class="col-sm-6">
                    <report-list id="report" :reports="reports"
                                 :report.sync="selectedReport"
                                 :initial-selected-report="initialReportName"/>
                </div>
            </div>

            <div v-if="showInstances">
                <instances :instances="metadata.instances"
                           :custom-style="childCustomStyle"
                           @selectedValues="handleInstancesValue"
                           @clearRun="clearRun"/>
            </div>
            <div v-if="showParameters" id="parameters" class="form-group row">
                <label for="params-component" class="col-sm-2 col-form-label text-right">Parameters</label>
                <parameter-list id="params-component" @paramsChanged="getParameterValues"
                                :params="parameterValues"></parameter-list>
            </div>
            <change-log v-if="showChangelog"
                        :changelog-type-options="metadata.changelog_types"
                        :custom-style="childCustomStyle"
                        @changelogMessage="handleChangeLogMessageValue"
                        @changelogType="handleChangeLogTypeValue">
            </change-log>
            <div v-if="showRunButton" id="run-form-group" class="form-group row">
                <div class="col-sm-2"></div>
                <div class="col-sm-6">
                    <button @click.prevent="runReport" class="btn" type="submit" :disabled="disableRun">
                        Run report
                    </button>
                    <div id="run-report-status" v-if="runningStatus" class="text-secondary mt-2">
                        {{ runningStatus }}
                        <a @click.prevent="$emit('changeTab')" href="#">View log</a>
                    </div>
                </div>
            </div>
        </form>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import {api} from "../../utils/api";
    import ParameterList from "./parameterList.vue"
    import ErrorInfo from "../errorInfo.vue";
    import Vue from "vue";
    import GitUpdateReports from "./gitUpdateReports.vue";
    import ReportList from "./reportList.vue";
    import ChangeLog from "./changeLog.vue";
    import Instances from "./instances.vue";

    export default Vue.extend({
        name: "runReport",
        props: [
            "metadata",
            "initialGitBranches",
            "initialReportName"
        ],
        components: {
            ErrorInfo,
            GitUpdateReports,
            ReportList,
            ParameterList,
            ChangeLog,
            Instances
        },
        data: () => {
            return {
                reports: [],
                selectedBranch: "",
                selectedCommitId: "",
                selectedReport: "",
                selectedInstances: {},
                error: "",
                defaultMessage: "",
                runningStatus: "",
                runningKey: "",
                disableRun: false,
                parameterValues: [],
                changeLogMessageValue: "",
                changeLogTypeValue: "",
                childCustomStyle: {label: "col-sm-2 text-right", control: "col-sm-6"}
            }
        },
        computed: {
            showReports() {
                return this.reports && this.reports.length;
            },
            showInstances() {
                return this.metadata.instances_supported && this.selectedReport;
            },
            showRunButton() {
                return !!this.selectedReport;
            },
            showParameters() {
                return this.selectedReport && this.parameterValues.length
            },
            showChangelog: function () {
                return !!this.selectedReport && this.metadata.changelog_types
            }
        },
        methods: {
            handleInstancesValue: function (instances) {
                this.selectedInstances = instances
            },
            handleChangeLogTypeValue: function (type: string) {
                this.changeLogTypeValue = type
            },
            handleChangeLogMessageValue: function (message: string) {
                this.changeLogMessageValue = message
            },
            getParameterValues(values, valid) {
                if (valid) {
                    this.parameterValues = [...values]
                }
                this.disableRun = !valid
            },
            branchSelected(newBranch) {
                this.selectedBranch = newBranch;
            },
            commitSelected(newCommit) {
                this.selectedCommitId = newCommit;
            },
            updateReports(newReports) {
                this.reports = newReports;
                this.selectedReport = "";
            },
            setParameters: function () {
                const commit = this.selectedCommitId ? `?commit=${this.selectedCommitId}` : ''
                api.get(`/report/${this.selectedReport}/config/parameters/${commit}`)
                    .then(({data}) => {
                        this.parameterValues = data.data
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error
                        this.defaultMessage = "An error occurred when getting parameters";
                    })
            },
            runReport() {
                //TODO: Add link to running report log on response, when implemented

                //Orderly server currently only accepts a single instance value, although the metadata endpoint supports
                //multiple instances - until multiple are accepted, send the selected instance value for instance with
                //greatest number of options. See VIMC-4561.
                let instances = {};
                if (this.metadata.instances_supported && this.metadata.instances &&
                    Object.keys(this.metadata.instances).length > 0) {
                    const instanceName = Object.keys(this.metadata.instances).sort((a, b) => this.metadata.instances[b].length - this.metadata.instances[a].length)[0];
                    const instance = this.selectedInstances[instanceName];
                    instances = Object.keys(this.metadata.instances).reduce((a, e) => ({[e]: instance, ...a}), {});
                }
                let params = {};
                params = this.parameterValues.reduce((params, param) => ({...params, [param.name]: param.value}), {});

                let changelog = null;
                if (this.changeLogMessageValue) {
                    changelog = {
                        message: this.changeLogMessageValue,
                        type: this.changeLogTypeValue
                    }
                }

                api.post(`/report/${this.selectedReport}/actions/run/`, {
                    instances,
                    params,
                    changelog,
                    gitBranch: this.selectedBranch,
                    gitCommit: this.selectedCommitId,
                })
                    .then(({data}) => {
                        this.disableRun = true;
                        this.runningKey = data.data.key;
                        this.runningStatus = "Run started";
                        this.error = "";
                        this.defaultMessage = "";

                        // select the latest run report by default in logs view
                        this.$emit('update:key', this.runningKey)
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred when running report";
                    });
            },
            clearRun() {
                this.runningStatus = "";
                this.runningKey = "";
                this.disableRun = false;
                this.changeLogMessageValue = ""
            }
        },
        mounted() {
            if (this.metadata.instances_supported) {
                const instances = this.metadata.instances;
                for (const key in instances) {
                    if (instances[key].length > 0) {
                        this.$set(this.selectedInstances, key, instances[key][0]);
                    }
                }
            }
        },
        watch: {
            selectedReport() {
                this.clearRun();
                if (this.selectedReport) {
                    this.setParameters();
                }
                this.parameterValues.length = 0
            },
            selectedInstances: {
                deep: true,
                handler() {
                    this.clearRun()
                }
            }
        }
    })
</script>
