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
            <div v-for="(options, name) in metadata.instances"
                 v-if="metadata.instances_supported && options.length > 1"
                 class="form-group row">
                <label :for="name" class="col-sm-2 col-form-label text-right">Database "{{ name }}"</label>
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
            <label class="col-sm-2 col-form-label text-right">Parameters</label>
          <parameter-list :params="parameterValues"></parameter-list>
          </div>
        </form>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import {api} from "../../utils/api";
    import ParameterList from "./parameterList"
    import ErrorInfo from "../errorInfo";
    import Vue from "vue";
    import ReportList from "./reportList";

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
                parameterValues: [{name: "", report_version: "", type: "", value: ""}]
            }
        },
        computed: {
           demoReport() {
             /*
              * This should be replaced to real data. The returned data from
              * "reports" isn't the right data type to be returned.
              */
             const data = []
             return data.push({name:'minimal', latest: '20210127-110409-c20c0e42'},
               {name:'minimal2', latest: '1'})
             return data
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
            showParameters() {
             return this.parameterValues && this.reports
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
          getParameters: function () {
            const latest = this.demoReport.map(report => report.latest)
            api.post('/run-report/parameters/', {latest})
                .then(({data}) => {
                  this.parameterValues = data.data
                })
                .catch((error) => {
                  this.error = error
                })
          }
        },
        mounted() {
          this.getParameters()

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
        demoReport: function () {
          this.getParameters()
        }
      }
    })
</script>
