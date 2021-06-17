<template>
    <div>
        <h2 id="add-report-header" class="pb-2">Add reports</h2>
        <div v-if="isReady">
            <div class="pb-4">
                <h2 id="git-header">Git</h2>
                <div>
                    <git-update-reports
                        :metadata="runReportMetadata"
                        :initial-branch="workflowMetadata.git_branch"
                        :initial-commit-id="workflowMetadata.git_commit"
                        :initial-branches="initialBranches"
                        @branchSelected="branchSelected"
                        @commitSelected="commitSelected"
                        @reportsUpdate="updateAvailableReports"
                    ></git-update-reports>
                </div>
            </div>
            <div class="pb-4">
                <h2 id="report-sub-header">Reports</h2>
                <div>
                    <div v-for="(report, index) in workflowMetadata.reports"
                         :id="`workflow-report-${index}`"
                         :key="index"
                         class="form-group row">

                        <label class="col-sm-2 col-form-label text-right">{{report.name}}</label>
                        <parameter-list
                            v-if="reportParameters[index].length > 0"
                            :params="reportParameters[index]"
                            @paramsChanged="paramsChanged(index, $event)"
                        ></parameter-list>
                        <div v-if="reportParameters[index].length === 0"
                             class="col-sm-6 col-form-label text-secondary">
                            <em>No parameters</em>
                        </div>
                        <div class="col-sm-2">
                            <button
                            type="button"
                            class="remove-workflow-report btn btn-primary"
                            @click="removeReport(index)"
                            >Remove report</button>
                        </div>
                        <hr/>
                    </div>

                    <div v-if="hasReports" id="add-report-div" class="form-group row">
                        <label for="workflow-report" class="col-sm-2 col-form-label text-right font-weight-bold">
                            Add report
                        </label>
                        <div class="col-sm-6">
                            <report-list id="workflow-report" :reports="reports"
                                         :report.sync="selectedReport"/>
                        </div>
                        <div class="col-sm-2">
                            <button :disabled="!selectedReport"
                                    id="add-report-button"
                                    type="button"
                                    class="px-2 btn btn-primary"
                                    @click="addReport">Add report</button>
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
import {
    Parameter,
    ReportWithDate,
    RunReportMetadata,
    RunWorkflowMetadata,
    WorkflowReportWithParams
} from "../../utils/types";
import {api} from "../../utils/api";
import GitUpdateReports from "../runReport/gitUpdateReports.vue";
import ReportList from "../runReport/reportList.vue";
import ParameterList from "../runReport/parameterList.vue";
import ErrorInfo from "../errorInfo.vue";
import {mapParameterArrayToRecord, mapRecordToParameterArray} from "../../utils/reports.ts";

//TODO: validation
//TODO: check UI for real VIMC report names - probably too long to really work in a LH column

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
}

interface Computed {
    isReady: boolean,
    hasReports: boolean,
    reportParameters: Parameter[][]
}

interface Methods {
    validateStep: () => void,
    branchSelected: (git_branch: string) => void,
    commitSelected: (git_commit: string) => void,
    updateAvailableReports: (reports: ReportWithDate) =>  void,
    addReport: () => void
    paramsChanged: (index: number, params: Parameter[]) => void
    removeReport: (index: number) => void
    updateWorkflowReports: (reports: WorkflowReportWithParams[]) => void
}

interface Data {
    runReportMetadata: RunReportMetadata | null,
    initialBranches:  string[] | null,
    reports: ReportWithDate[],
    selectedReport: string,
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
        ReportList,
        ParameterList,
        ErrorInfo
    },
    data() {
        return {
            runReportMetadata: null,
            initialBranches: null,
            reports: [],
            selectedReport: "",
            error: "",
            defaultMessage: ""
        }
    },
    computed: {
        isReady: function() {
            return !!this.runReportMetadata && !!this.workflowMetadata;
        },
        hasReports: function() {
            return this.reports.length > 0;
        },
        reportParameters: function() {
            return this.workflowMetadata.reports.map(r => r.params ? mapRecordToParameterArray(r.params) : []);
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
        updateAvailableReports(reports) {
            //TODO: when available reports are updated, we may have an invalid workflow - may not have reports or parameters
            //in the report set in the new commit, so may need remove reports or parameters from wf metadata
            this.reports = reports;
        },
        addReport() {
            const commit = this.workflowMetadata.git_commit ? `?commit=${this.workflowMetadata.git_commit}` : '';
            api.get(`/report/${this.selectedReport}/parameters/${commit}`)
                .then(({data}) => {
                    const parameterValues = mapParameterArrayToRecord(data.data);
                    const newReports = [
                        ...this.workflowMetadata.reports,
                        {
                            name: this.selectedReport,
                            params: parameterValues
                        }
                    ];
                    this.updateWorkflowReports(newReports);

                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage = "An error occurred when getting parameters";
                });

        },
        removeReport(index: number) {
            const newReports = [...this.workflowMetadata.reports]
            newReports.splice(index, 1);
            this.updateWorkflowReports(newReports);
        },
        paramsChanged(index: number, params: Parameter[]) {
            const newReports = [
                ...this.workflowMetadata.reports,
            ];
            newReports[index] = {...newReports[index], params: mapParameterArrayToRecord(params)};
            this.updateWorkflowReports(newReports);
        },
        updateWorkflowReports(reports: WorkflowReportWithParams[]) {
            this.$emit("update", {reports: reports});
        }
    },
    mounted() {
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
})
</script>
