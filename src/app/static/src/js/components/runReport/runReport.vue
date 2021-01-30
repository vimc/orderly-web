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
            <div id="run-form-group" class="form-group row">
                <div class="col-sm-2"></div>
                <div class="col-sm-6">
                    <button v-if="showRunButton" @click.prevent="runReport" class="btn" type="submit">Run report</button>
                    <div id="run-report-status" v-if="runningStatus" class="text-secondary mt-2">
                        {{runningStatus}}
                        <a @click.prevent="checkStatus" href="#">Check status</a>
                    </div>
                </div>
            </div>
        </form>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo";
    import ReportList from "./reportList";

    export default {
        name: "runReport",
        props: [
            "metadata",
            "gitBranches",
        ],
        components: {
            ErrorInfo,
            ReportList
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
                runningKey: ""
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
            runReport() {
                //TODO: Include parameters and changelog message when implemented
                //TODO: Add link to running report log on response, when implemented
                //TODO: confirm instances format and provide instance(s) - orderly web server expects a single string,
                //      but our metadata suggests should be returning a map. Check which we've settled on.
                const params = {
                    ref: this.selectedCommitId,
                    instance: ""
                };
                api.post(`/report/${this.selectedReport}/actions/run/`, {}, {params})
                    .then(({data}) => {
                        this.error = "";
                        this.runningKey = data.data.key;
                        this.runningStatus = "Run started";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "Error when running report";
                    });
            },
            checkStatus() {
                //TODO: This can be removed, along with the check status link once the logging page is in - but is a
                //handy diagnostic for now
                api.get(`/report/${this.selectedReport}/actions/status/${this.runningKey}/`)
                    .then(({data}) => {
                        this.runningStatus = `Running status: ${data.data.status}`;
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "Error when fetching report status";
                    });
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
                        this.selectedInstances[key] = instances[key][0]
                    }
                }
            }
        },
        watch: {
            selectedReport() {
                this.runningStatus = "";
            },
            selectedInstances() {
                this.runningStatus = "";
            }
        }
    }
</script>
