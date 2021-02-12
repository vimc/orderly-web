<template>
    <div>
        <form class="mt-3">
            <div v-if="metadata.git_supported" id="git-branch-form-group" class="form-group row">
                <label for="git-branch" class="col-sm-2 col-form-label text-right">Git branch</label>
                <div class="col-sm-6">
                    <select class="form-control" id="git-branch" v-model="selectedBranch" @change="changedBranch">
                        <option v-for="branch in gitBranches" :value="branch">{{ branch }}</option>
                    </select>
                </div>
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
                    <report-list id="report" :reports="reports" :report.sync="selectedReport"/>
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
                <parameter-list id="params-component" @getParams="getParameterValues"
                                :params="parameterValues"></parameter-list>
            </div>
            <div v-if="showChangelog">
                <div id="changelog-message" class="form-group row">
                    <label for="changelogMessage" class="col-sm-2 col-form-label text-right">Changelog Message</label>
                    <div class="col-sm-6">
                        <textarea class="form-control" id="changelogMessage" v-model="changeLogMessageValue"
                                  rows="2"></textarea>
                    </div>
                </div>
                <div id="changelog-type" class="form-group row">
                    <label for="changelogType" class="col-sm-2 col-form-label text-right">Changelog Type</label>
                    <div class="col-sm-6">
                        <select class="form-control" id="changelogType" v-model="changeLogTypeValue">
                            <option v-for="option in metadata.changelog_types" :value="option">
                                {{ option }}
                            </option>
                        </select>
                    </div>
                </div>
            </div>
            <div v-if="showRunButton" id="run-form-group" class="form-group row">
                <div class="col-sm-2"></div>
                <div class="col-sm-6">
                    <button @click.prevent="runReport" class="btn" type="submit" :disabled="disableRun">
                        Run report
                    </button>
                    <div id="run-report-status" v-if="runningStatus" class="text-secondary mt-2">
                        {{ runningStatus }}
                        <a @click.prevent="checkStatus" href="#">Check status</a>
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

    export default Vue.extend({
        name: "runReport",
        props: [
            "metadata",
            "gitBranches",
        ],
        components: {
            ErrorInfo,
            ReportList,
            ParameterList
        },
        data: () => {
            return {
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
                return this.selectedReport
            }
        },
        methods: {
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
            getParameterValues(values) {
                if (values) {
                    this.parameterValues.forEach((param, key) => {
                        if (values[key].name == param.name) {
                            param.value = values[key].value
                        }
                    })
                }
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
                //TODO: Include parameters and changelog message when implemented
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

                api.post(`/report/${this.selectedReport}/actions/run/`, {
                    instances: instances,
                    params: {}, //TODO mrc-2167
                    gitBranch: this.selectedBranch,
                    gitCommit: this.selectedCommitId,
                })
                    .then(({data}) => {
                        this.disableRun = true;
                        this.runningKey = data.data.key;
                        this.runningStatus = "Run started";
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred when running report";
                    });
            },
            checkStatus() {
                //TODO: This can be removed, along with the check status link once the logging page is in - but is a
                //handy diagnostic for now
                api.get(`/report/${this.selectedReport}/actions/status/${this.runningKey}/`)
                    .then(({data}) => {
                        this.runningStatus = `Running status: ${data.data.status}`;
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred when fetching report status";
                    });
                this.disableRun = false;
            },
            clearRun() {
                this.runningStatus = "";
                this.runningKey = "";
                this.disableRun = false;
            }
        },
        mounted() {
            if (this.metadata.git_supported) {
                this.selectedBranch = this.gitBranches[0];
                this.changedBranch();
            } else {
                this.updateReports();
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
