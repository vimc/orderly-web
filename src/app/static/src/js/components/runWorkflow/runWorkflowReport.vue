<template>
    <div>
        <b-alert
                :show="!!workflowRemovals"
                dismissible
                variant="warning"
                class="col-sm-10"
                @dismissed="workflowRemovals=null">
            The following items are not present in this git commit and have been removed from the workflow:
            <ul class="py-0 my-0 ml-2" :style="{listStyleType: 'disc'}">
                <li v-for="item in workflowRemovals">{{item}}</li>
            </ul>
        </b-alert>
        <h2 id="add-report-header" class="pb-2">Add reports</h2>
        <div v-if="isReady">
            <div class="pb-4">
                <h3 id="git-header">Git</h3>
                <div>
                    <git-update-reports
                        :report-metadata="runReportMetadata"
                        :initial-branch="workflowMetadata.git_branch"
                        :initial-commit-id="workflowMetadata.git_commit"
                        :initial-branches="initialBranches"
                        @branchSelected="branchSelected"
                        @commitSelected="commitSelected"
                        @reportsUpdate="updateAvailableReportsFromGit"
                    ></git-update-reports>
                </div>
            </div>
            <div class="pb-4" id="workflow-reports">
                <h3 id="report-sub-header">Reports</h3>
                <div id="choose-import-from">
                    <label class="col-sm-2 col-form-label text-right"></label>
                    <div class="btn-group btn-group-toggle" data-toggle="buttons">
                        <label id="choose-from-list-label" class="btn btn-outline-primary btn-toggle shadow-none active">
                            <input type="radio" id="choose-from-list"
                                   v-model="reportsOrigin" value="list"
                                   autocomplete="off" checked> Choose from list
                        </label>
                        <label id="import-from-csv-label" class="btn btn-outline-primary btn-toggle shadow-none">
                            <input type="radio" id="import-from-csv"
                                   v-model="reportsOrigin" value="csv"
                                   autocomplete="off"> Import from csv
                        </label>
                    </div>
                </div>
                <div v-for="(report, index) in workflowMetadata.reports "
                     :id="`workflow-report-${index}`"
                     :key="index"
                     class="form-group row pt-4">

                    <label class="col-sm-2 col-form-label text-right text-truncate" :title="report.name">{{report.name}}</label>
                    <parameter-list
                        v-if="reportParameters[index].length > 0"
                        :params="reportParameters[index]"
                        @paramsChanged="(...eventArgs) => paramsChanged(index, ...eventArgs)"
                    ></parameter-list>
                    <div v-if="reportParameters[index].length === 0"
                         class="col-sm-6 col-form-label text-secondary no-parameters">
                        <em>No parameters</em>
                    </div>
                    <div class="col-sm-2">
                        <button
                            type="button"
                            class="remove-report-button btn btn-primary"
                            @click="removeReport(index)"
                        >Remove report</button>
                    </div>
                    <hr/>
                </div>
                <div v-if="showImportFromCsv" id="show-import-csv" class="pt-4">
                    <label class="col-sm-2 col-form-label text-right"></label>
                    <div class="custom-file col-sm-6">
                        <input type="file" class="custom-file-input"
                               v-on:change="handleImportedFile($event)"
                               accept="text/csv"
                               id="import-csv"
                               lang="en">
                        <label class="custom-file-label" for="import-csv">{{ importedFilename }}</label>
                    </div>
                </div>
                <div v-if="!showImportFromCsv" id="show-report-list" class="pt-4">
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
import Vue from "vue";
import {BAlert} from "bootstrap-vue";
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
import {AxiosResponse} from "axios";

interface Props {
    workflowMetadata: RunWorkflowMetadata
}

interface Computed {
    isReady: boolean,
    hasReports: boolean,
    reportParameters: Parameter[][],
    stepIsValid: boolean,
    showImportFromCsv: boolean
}

interface Methods {
    branchSelected: (git_branch: string) => void,
    commitSelected: (git_commit: string) => void,
    getParametersApiCall: (report: string) => Promise<AxiosResponse>,
    updateAvailableReportsFromGit: (reports: ReportWithDate[]) =>  void,
    addReport: () => void,
    paramsChanged: (index: number, params: Parameter[], valid: boolean) => void,
    removeReport: (index: number) => void,
    updateWorkflowReports: (reports: WorkflowReportWithParams[]) => void,
    initialValidValue: (report: WorkflowReportWithParams) => boolean,
    getRunReportMetadata: () => void
    validateWorkflow: () => void
    handleImportedFile: (event: Event) => void
}

interface Data {
    runReportMetadata: RunReportMetadata | null,
    initialBranches:  string[] | null,
    reports: ReportWithDate[],
    selectedReport: string,
    error: string,
    validationError: string,
    defaultMessage: string,
    workflowRemovals: string[] | null,
    reportsValid: boolean[],
    reportsOrigin: "csv" | "list",
    importedFilename: string,
    importedFile: string
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
        ErrorInfo,
        BAlert
    },
    data() {
        return {
            runReportMetadata: null,
            initialBranches: null,
            reports: [],
            selectedReport: "",
            error: "",
            validationError: "",
            defaultMessage: "",
            workflowRemovals: null,
            reportsValid: [],
            importedFilename: "",
            importedFile: "",
            reportsOrigin: "list",
        }
    },
    computed: {
        showImportFromCsv() {
            return this.reportsOrigin === "csv"
        },
        isReady: function() {
            return !!this.runReportMetadata && !!this.workflowMetadata;
        },
        hasReports: function() {
            return this.reports.length > 0;
        },
        reportParameters: function() {
            return this.workflowMetadata.reports.map(r => r.params ? mapRecordToParameterArray(r.params) : []);
        },
        stepIsValid: function() {
            return (this.reportsValid.length > 0) && (this.reportsValid.every(v => v));
        }
    },
    methods: {
        handleImportedFile(event) {
            const target = event.target as HTMLInputElement;
            if (target.files.length) {
                this.importedFilename = target.files[0].name;
                this.importedFile = target.files[0];

                this.validateWorkflow()
            }
        },
        branchSelected(git_branch: string) {
            this.$emit("update", {git_branch});
        },
        commitSelected(git_commit: string) {
            this.$emit("update", {git_commit})
        },
        getParametersApiCall(report: string) {
            const commit = this.workflowMetadata.git_commit ? `?commit=${this.workflowMetadata.git_commit}` : '';
            return api.get(`/report/${report}/config/parameters/${commit}`);
        },
        async updateAvailableReportsFromGit(reports: ReportWithDate[]) {
            this.reports = reports;

            // We may now have an invalid workflow - it may contain reports or parameters not in the newly selected commit
            // - remove obsolete reports or params and notify user
            // 1. Check reports
            const removals: string[] = [];
            let newParamsAdded = false;
            const addToRemovals = (s: string) => {
                //avoid duplicates
                if (!removals.includes(s)) {
                    removals.push(s);
                }
            };

            const newReports = [];
            const availableReportNames = reports.map(report => report.name);
            const validityIndexRemovals = [];
            this.workflowMetadata.reports.forEach((report: WorkflowReportWithParams, index: number) => {
                if (availableReportNames.includes(report.name)) {
                    newReports.push({...report, params: {...report.params}});
                } else {
                    addToRemovals(`Report '${report.name}'`);
                    validityIndexRemovals.push(index);
                }
            });

            //Deal with removing valid flags by index in reverse to avoid moving index bug
            for(let i = validityIndexRemovals.length-1; i >=0; i--) {
                this.reportsValid.splice(validityIndexRemovals[i], 1);
            }

            // 2. Check parameters
            const calls = newReports.map((report, index) => {
                return this.getParametersApiCall(report.name)
                    .then(({data}) => {
                        const newParameterValues = mapParameterArrayToRecord(data.data);
                        // Check for parameters in metadata not in fetched params
                        for (const paramName of Object.keys(report.params)) {
                            if (!Object.keys(newParameterValues).includes(paramName)) {
                                delete report.params[paramName];
                                addToRemovals(`Parameter '${paramName}' in report '${report.name}'`);

                                // If we have removed the last param from a report it becomes valid
                                if (Object.keys(report.params).length === 0) {
                                    this.$set(this.reportsValid, index, true);
                                }
                            }
                        }

                        // Check for parameters in fetched params not in metadata
                        for (const paramName of Object.keys(newParameterValues)) {
                            if (!Object.keys(report.params).includes(paramName)) {
                                report.params[paramName] = newParameterValues[paramName];
                                newParamsAdded = true;
                            }
                        }

                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred when refreshing parameters";
                    });
            });

            await Promise.all(calls);
            this.workflowRemovals = removals.length > 0 ? removals : null;
            if ((removals.length > 0) || newParamsAdded) {
                this.updateWorkflowReports(newReports);
            }

            if (this.selectedReport) {
                const newReportNames = reports.map(report => report.name);
                if (!newReportNames.includes(this.selectedReport)) {
                    this.selectedReport = "";
                }
            }
        },
        addReport() {
            this.getParametersApiCall(this.selectedReport)
                .then(({data}) => {
                    const parameterValues = mapParameterArrayToRecord(data.data);
                    const newReport = {
                        name: this.selectedReport,
                        params: parameterValues
                    };
                    const newReports = [
                        ...this.workflowMetadata.reports,
                        newReport
                    ];
                    this.updateWorkflowReports(newReports);
                    this.reportsValid.push(this.initialValidValue(newReport));
                    this.selectedReport = "";
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage = "An error occurred when getting parameters";
                });
        },
        removeReport(index: number) {
            const newReports = [...this.workflowMetadata.reports];
            newReports.splice(index, 1);
            this.reportsValid.splice(index, 1);
            this.updateWorkflowReports(newReports);
        },
        paramsChanged(index: number, params: Parameter[], valid: boolean) {
            const newReports = [
                ...this.workflowMetadata.reports,
            ];
            newReports[index] = {...newReports[index], params: mapParameterArrayToRecord(params)};
            this.updateWorkflowReports(newReports);

            this.$set(this.reportsValid, index, valid);

        },
        updateWorkflowReports(reports: WorkflowReportWithParams[]) {
            this.$emit("update", {reports: reports});
        },
        initialValidValue(report: WorkflowReportWithParams) {
            // report with no parameters is by definition valid, those with params will notify via parameterList
            return !(report.params && Object.keys(report.params).length > 0);
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
        },
        validateWorkflow() {
            const formData = new FormData()
            formData.append("file", this.importedFile, this.importedFilename)
            formData.append("git_branch", this.workflowMetadata.git_branch)
            formData.append("git_commit", this.workflowMetadata.git_commit)

            api.post(`/workflow/validate`, formData)
                .then(({data}) => {
                    this.updateWorkflowReports(data.data);
                    this.validationError = "";
                })
                .catch((error) => {
                    this.validationError = error;
                });
        }
    },
    mounted() {
        this.getRunReportMetadata();
        this.reportsValid = this.workflowMetadata.reports.map(r => this.initialValidValue(r));
    },
    watch: {
        stepIsValid(newVal) {
            this.$emit("valid", newVal);
        }
    }
})
</script>
