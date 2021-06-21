<template>
    <div>
        <h2>Run a report</h2>
        <form class="mt-3">
            <div v-if="metadata.git_supported" id="git-branch-form-group" class="form-group row">
                <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
                <div class="col-sm-6">
                    <select class="form-control" id="git-branch" v-model="selectedBranch" @change="changedBranch">
                        <option v-for="branch in gitBranches" :value="branch">{{ branch }}</option>
                    </select>
                </div>
                <button @click.prevent="refreshGit"
                    id="git-refresh-btn"
                    class="btn col-sm-1"
                    :disabled="gitRefreshing"
                    type="submit">
                    {{refreshGitText}}
                </button>
            </div>
            <div v-if="showCommits" id="git-commit-form-group" class="form-group row">
                <label for="git-commit" class="col-sm-2 col-form-label text-right">Git commit</label>
                <div class="col-sm-6">
                    <select class="form-control" id="git-commit" v-model="selectedCommitId" @change="changedCommit">
                        <option v-for="commit in gitCommits" :value="commit.id">
                            {{ commit.id }} ({{ commit.date_time }})
                        </option>
                    </select>
                </div>
            </div>
            <div v-if="showReports" id="report-form-group" class="form-group row">
                <label for="report" class="col-sm-2 col-form-label text-right">Report</label>
                <div class="col-sm-6">
                    <report-list id="report" :reports="reports"
                                 :report.sync="selectedReport"
                                 :initial-selected-report="initialReportName"/>
                </div>
            </div>
            <template v-if="showInstances">
                <div v-for="(options, name) in metadata.instances" v-if="options.length > 1" class="form-group row">
                    <label :for="name" class="col-sm-2 col-form-label text-right">Database "{{ name }}"</label>
                    <div class="col-sm-6">
                        <select class="form-control" :id="name" v-model="selectedInstances[name]">
                            <option v-for="option in options" :value="option">
                                {{ option }}
                            </option>
                        </select>
                    </div>
                </div>
            </template>
            <div v-if="showParameters" id="parameters" class="form-group row">
                <label for="params-component" class="col-sm-2 col-form-label text-right">Parameters</label>
                <parameter-list id="params-component" @paramsChanged="getParameterValues"
                                :params="parameterValues"></parameter-list>
            </div>
            <div v-if="showChangelog">
            <change-log :show-change-message="showChangeMessage"
                        :report-metadata="metadata"
                        @changelogMessage="handleChangeLogMessageValue"
                        @changelogType="handleChangeLogTypeValue">
            </change-log>
            </div>
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
    import ReportList from "./reportList.vue";
    import ChangeLog from "./changeLog.vue";

    export default Vue.extend({
        name: "runReport",
        props: [
            "metadata",
            "initialGitBranches",
            "initialReportName"
        ],
        components: {
            ErrorInfo,
            ReportList,
            ParameterList,
            ChangeLog
        },
        data: () => {
            return {
                gitRefreshing: false,
                gitBranches: [],
                gitCommits: [],
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
                changeLogTypeValue: ""
            }
        },
        computed: {
            refreshGitText(){
                return this.gitRefreshing ? 'Fetching...' : 'Refresh git'
            },
            showCommits() {
                return this.gitCommits && this.gitCommits.length;
            },
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
                return !!this.selectedReport
            },
            showChangeMessage: function () {
                return this.metadata.changelog_types
            }
        },
        methods: {
            handleChangeLogTypeValue: function (type: string) {
                this.changeLogTypeValue = type
            },
            handleChangeLogMessageValue: function (message: string) {
                this.changeLogMessageValue = message
            },
            refreshGit: function () {
                this.gitRefreshing = true
                api.get('/git/fetch/')
                    .then(({data}) => {
                        this.gitRefreshing = false
                        this.gitBranches = data.data.map(branch => branch.name)
                    })
                    .catch((error) => {
                        this.gitRefreshing = false
                        this.error = error;
                        this.defaultMessage = "An error occurred refreshing Git";
                    });
            },
            changedBranch() {
                api.get(`/git/branch/${this.selectedBranch}/commits/`)
                    .then(({data}) => {
                        this.gitCommits = data.data;
                        if (this.gitCommits.length) {
                            this.selectedCommitId = this.gitCommits[0].id;
                            this.changedCommit();
                        }
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching Git commits";
                    });
            },
            getParameterValues(values, valid) {
                if (valid) {
                    this.parameterValues = [...values]
                }
                this.disableRun = !valid
            },
            changedCommit() {
                this.updateReports();
            },
            updateReports() {
                this.reports = [];
                const query = this.metadata.git_supported ? `?branch=${this.selectedBranch}&commit=${this.selectedCommitId}` : '';
                api.get(`/reports/runnable/${query}`)
                    .then(({data}) => {
                        this.reports = data.data;
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching reports";
                    });
            },
            setParameters: function () {
                const commit = this.selectedCommitId ? `?commit=${this.selectedCommitId}` : ''
                api.get(`/report/${this.selectedReport}/parameters/${commit}`)
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
                    const instance = this.selectedInstances[instanceName];instances = Object.keys(this.metadata.instances).reduce((a, e) => ({[e]: instance, ...a}), {});
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
            if (this.metadata.git_supported) {
                this.gitBranches = [...this.initialGitBranches]
                this.selectedBranch = this.gitBranches.length ? this.gitBranches[0] : [];
                this.changedBranch();
            } else {
                this.updateReports();
            }
            if(this.metadata.changelog_types) {
                this.changeLogTypeValue = this.metadata.changelog_types[0]
            }

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
            gitBranches(){
                this.gitCommits = [];
                this.reports = [];
                this.selectedBranch = this.gitBranches.length ? this.gitBranches[0] : [];
                this.selectedCommitId = "";
                this.selectedReport = "";
                this.changedBranch()
            },
            selectedReport() {
                this.clearRun();
                if (this.selectedReport) {
                    this.setParameters()
                }
                this.parameterValues.length = 0
            },
            selectedInstances: {
                deep: true,
                handler() {
                    this.clearRun();
                }
            }
        }
    })
</script>
