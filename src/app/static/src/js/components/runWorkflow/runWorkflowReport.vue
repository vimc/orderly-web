<template>
    <div>
        <h2 id="add-report-header" class="pb-2">Add reports</h2>
        <div v-if="isReady">
            <div class="pb-4">
                <h2 id="git-header">Git</h2>
                <div>
                    <git-update-reports
                        :report-metadata="runReportMetadata"
                        :initial-branch="workflowMetadata.git_branch"
                        :initial-commit-id="workflowMetadata.git_commit"
                        :initial-branches="initialBranches"
                        @branchSelected="branchSelected"
                        @commitSelected="commitSelected"
                        @reportsUpdate="updateReports"
                    ></git-update-reports>
                </div>
            </div>
            <div class="pb-4">
                <h2 id="report-sub-header">Reports</h2>
                <div>
                    <div id="preprocess-div" class="form-group row">
                        <label class="col-sm-2 col-form-label text-right">Preprocess</label>
                        <div>
                        <div class="form-group row">
                            <label for="n-min" class="col-sm-4 col-form-label text-right">nmin:</label>
                            <div class="col-sm-8 input-group">
                                <input type="text" class="form-control mr-2" id="n-min" placeholder="7">
                                <button id="workflow-remove-button" type="button" class="px-2 btn btn-primary">Remove report</button>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="n-max" class="col-sm-4 col-form-label text-right">nmax:</label>
                            <div class="col-sm-4">
                                <input type="text" class="form-control" id="n-max" placeholder="16">
                            </div>
                        </div>
                        </div>
                    </div>

                    <div id="add-report-div" class="form-group row">
                        <label for="workflow-report" class="col-sm-2 col-form-label text-right">Add report</label>
                        <div class="col-sm-4 input-group">
                            <input type="text" @input="validateStep" class="form-control mr-2" id="workflow-report" placeholder="16">
                            <button id="add-report-button" type="button" class="px-2 btn btn-primary">Add report</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import {ReportWithDate, RunReportMetadata, RunWorkflowMetadata} from "../../utils/types";
import {api} from "../../utils/api";
import GitUpdateReports from "../runReport/gitUpdateReports.vue";
import ErrorInfo from "../errorInfo.vue";

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
}

interface Computed {
    isReady: boolean
}

interface Methods {
    getRunReportMetadata: () => void,
    validateStep: () => void,
    branchSelected: (git_branch: string) => void,
    commitSelected: (git_commit: string) => void,
    updateReports: (reports: ReportWithDate) =>  void
}

interface Data {
    runReportMetadata: RunReportMetadata | null,
    initialBranches:  string[] | null,
    reports: ReportWithDate[],
    error: string,
    defaultMessage: string
}

export default Vue.extend<Data, Methods, Computed, Props>({
    name: "runWorkflowReport",
    props: {
        workflowMetadata: Object
    },
    components: {
        GitUpdateReports,
        ErrorInfo
    },
    data() {
        return {
            runReportMetadata: null,
            initialBranches: null,
            reports: [],
            error: "",
            defaultMessage: ""
        }
    },
    computed: {
        isReady: function() {
            return !!this.runReportMetadata && !!this.workflowMetadata;
        }
    },
    methods: {
        validateStep: function () {
            /**
             *  Valid step should be set to true or false once validation is complete
             */
            this.$emit("valid", true)
        },
        branchSelected(git_branch: string) {
            this.$emit("update", {git_branch});
        },
        commitSelected(git_commit: string) {
            this.$emit("update", {git_commit})
        },
        updateReports(reports) {
            this.reports = reports;
            console.log(`Updating reports with ${JSON.stringify(reports)}`); //TODO: Update reports list
        },
        getRunReportMetadata() {
            api.get(`/report/run-metadata`)
                .then(({data}) => {
                    this.initialBranches = data.data.git_branches;
                    this.runReportMetadata = data.data.metadata;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage = "An error occurred fetching run report metadata";
                });
        }
    },
    mounted() {
        this.getRunReportMetadata();
    }
})
</script>
