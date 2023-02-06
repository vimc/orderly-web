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
                <li v-for="item in workflowRemovals" :key="item">
                    {{ item }}
                </li>
            </ul>
        </b-alert>
        <h2 id="add-report-header" class="pb-2">
            Add reports
        </h2>
        <div v-if="isReady">
            <div class="pb-4">
                <h3 id="git-header" v-if="runReportMetadata && runReportMetadata.git_supported">
                    Git
                </h3>
                <div>
                    <git-update-reports
                        :report-metadata="runReportMetadata"
                        :initial-branch="workflowMetadata.git_branch"
                        :initial-commit-id="workflowMetadata.git_commit"
                        :initial-branches="initialBranches"
                        :show-all-reports="true"
                        @branchSelected="branchSelected"
                        @commitSelected="commitSelected"
                        @reportsUpdate="updateAvailableReportsFromGit"></git-update-reports>
                </div>
            </div>
            <div id="workflow-reports" class="pb-4">
                <h3 id="report-sub-header">
                    Reports
                </h3>
                <div v-if="importFromCsvIsEnabled" id="choose-import-from">
                    <div class="col-sm-2 d-inline-block"></div>
                    <div class="btn-group btn-group-toggle" data-toggle="buttons">
                        <label id="choose-from-list-label"
                               class="btn btn-outline-primary btn-toggle shadow-none"
                               :class="reportsOrigin === 'list' ? 'active' : ''">
                            <input id="choose-from-list" v-model="reportsOrigin"
                                   type="radio" value="list"
                                   autocomplete="off"> Choose from list
                        </label>
                        <label id="import-from-csv-label"
                               class="btn btn-outline-primary btn-toggle shadow-none"
                               :class="reportsOrigin === 'csv' ? 'active' : ''">
                            <input id="import-from-csv" v-model="reportsOrigin"
                                   type="radio" value="csv"
                                   autocomplete="off"> Import from csv
                        </label>
                    </div>
                </div>
                <div v-if="showImportFromCsv" id="show-import-csv" class="pt-4">
                    <div class="col-sm-2 d-inline-block"></div>
                    <div class="custom-file col-sm-6">
                        <input id="import-csv" type="file"
                               class="custom-file-input"
                               accept="text/csv"
                               lang="en"
                               @change="handleImportedFile($event)"
                               @click="handleClickImport($event)">
                        <label class="custom-file-label" for="import-csv">{{ importedFilename }}</label>
                    </div>
                    <div>
                        <div class="col-sm-2 d-inline-block"></div>
                        <b-alert id="import-validation-errors"
                                 :show="!!validationErrors.length"
                                 dismissible
                                 variant="danger"
                                 class="col-sm-6 mt-4 d-inline-block"
                                 @dismissed="validationErrors=[]">
                            Failed to import from csv. The following issues were found:
                            <ul class="py-0 my-0 ml-2" :style="{listStyleType: 'disc'}">
                                <li v-for="e in validationErrors" :key="e.message" class="import-validation-error">
                                    {{ e.message }}
                                </li>
                            </ul>
                        </b-alert>
                    </div>
                </div>
                <div v-for="(report, index) in workflowMetadata.reports"
                     :id="`workflow-report-${index}`"
                     :key="index"
                     class="form-group row pt-4">

                    <label class="col-sm-2 col-form-label text-right text-truncate"
                           :title="report.name">{{ report.name }}</label>
                    <parameter-list
                        v-if="reportParameters[index].length > 0"
                        :params="reportParameters[index]"
                        @paramsChanged="(...eventArgs) => paramsChanged(index, ...eventArgs)"></parameter-list>
                    <div v-if="reportParameters[index].length === 0"
                         class="col-sm-6 col-form-label text-secondary no-parameters">
                        <em>No parameters</em>
                    </div>
                    <div class="col-sm-2">
                        <button
                            type="button"
                            class="remove-report-button btn btn-primary"
                            @click="removeReport(index)">Remove report
                        </button>
                    </div>
                    <hr/>
                </div>
                <div v-if="!showImportFromCsv" id="show-report-list" class="pt-4">
                    <div v-if="hasReports" id="add-report-div" class="form-group row">
                        <label for="workflow-report" class="col-sm-2 col-form-label text-right font-weight-bold">
                            Add report
                        </label>
                        <div class="col-sm-6">
                            <report-list id="workflow-report" :reports="reports"
                                         :selected-report.sync="selectedReport"/>
                        </div>
                        <div class="col-sm-2">
                            <button id="add-report-button"
                                    :disabled="!selectedReport"
                                    type="button"
                                    class="px-2 btn btn-primary"
                                    @click="addReport">Add report
                            </button>
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
    import {BAlert} from "bootstrap-vue/esm/components/alert";
    import {
        ErrorResponse,
        Parameter,
        ReportWithDate, RunnerRootState,
        RunReportMetadataDependency,
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
    import {switches} from '../../featureSwitches.ts';
    import {session} from "../../utils/session";
    import {mapState} from "vuex";

    interface Props {
        workflowMetadata: RunWorkflowMetadata
    }

    interface Computed {
        isReady: boolean,
        hasReports: boolean,
        reportParameters: Parameter[][],
        stepIsValid: boolean,
        showImportFromCsv: boolean,
        runReportMetadata: RunReportMetadataDependency | null,
        initialBranches: string[]
    }

    interface Methods {
        branchSelected: (git_branch: string) => void,
        commitSelected: (git_commit: string) => void,
        getParametersApiCall: (report: string) => Promise<AxiosResponse>,
        updateAvailableReportsFromGit: (reports: ReportWithDate[]) => void,
        addReport: () => void,
        paramsChanged: (index: number, params: Parameter[], valid: boolean) => void,
        removeReport: (index: number) => void,
        updateWorkflowReports: (reports: WorkflowReportWithParams[]) => void,
        initialValidValue: (report: WorkflowReportWithParams) => boolean,
        validateWorkflow: () => void
        handleImportedFile: (event: Event) => void
        handleClickImport: (event: Event) => void
        removeImportedFile: () => void
    }

    interface Data {
        reports: ReportWithDate[],
        selectedReport: ReportWithDate,
        error: string,
        validationErrors: ErrorResponse[],
        defaultMessage: string,
        workflowRemovals: string[] | null,
        reportsValid: boolean[],
        reportsOrigin: string,
        importedFilename: string,
        importedFile: object | null
        importFromCsvIsEnabled: boolean
        isImportedReports: boolean
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "RunWorkflowReport",
        components: {
            GitUpdateReports,
            ReportList,
            ParameterList,
            ErrorInfo,
            BAlert
        },
        props: {
            workflowMetadata: Object
        },
        data() {
            return {
                reports: [],
                selectedReport: null,
                error: "",
                validationErrors: [],
                defaultMessage: "",
                workflowRemovals: null,
                reportsValid: [],
                importedFilename: "",
                importedFile: null,
                reportsOrigin: session.getSelectedWorkflowReportSource() || "list",
                importFromCsvIsEnabled: switches.workFlowReport,
                isImportedReports: false
            }
        },
        computed: {
            ...mapState({
                initialBranches: (state: RunnerRootState) => state.git.branches,
                runReportMetadata: (state: RunnerRootState) => state.git.metadata
            }),
            showImportFromCsv() {
                return this.reportsOrigin === "csv"
            },
            isReady: function () {
                return !!this.runReportMetadata && !!this.workflowMetadata;
            },
            hasReports: function () {
                return this.reports.length > 0;
            },
            reportParameters: function () {
                return this.workflowMetadata.reports.map(r => r.params ? mapRecordToParameterArray(r.params) : []);
            },
            stepIsValid: function () {
                return (this.reportsValid.length > 0) && (this.reportsValid.every(v => v));
            }
        },
        watch: {
            stepIsValid(newVal) {
                this.$emit("valid", newVal);
            },
            reportsOrigin(newVal) {
                session.setSelectedWorkflowReportSource(newVal);
            }
        },
        beforeMount() {
            this.reportsValid = this.workflowMetadata.reports.map(r => this.initialValidValue(r));
        },
        methods: {
            removeImportedFile: function () {
                if (this.isImportedReports) {
                    this.importedFile = null
                    this.importedFilename = ""
                    this.isImportedReports = false
                }
            },
            handleClickImport: function (event: Event) {
                // Clear import value to allow successive imports of same file
                (event.target as HTMLInputElement).value = null;
            },
            handleImportedFile(event) {
                const target = event.target as HTMLInputElement;
                if (target.files.length) {
                    this.importedFilename = target.files[0].name;
                    this.importedFile = target.files[0];

                    this.validateWorkflow()
                }
            },
            branchSelected(git_branch: string) {
                this.validationErrors = [];
                this.$emit("update", {git_branch});
            },
            commitSelected(git_commit: string) {
                this.validationErrors = [];
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
                for (let i = validityIndexRemovals.length - 1; i >= 0; i--) {
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
                    if (!newReportNames.includes(this.selectedReport.name)) {
                        this.selectedReport = null;
                    }
                }
            },
            addReport() {
                this.getParametersApiCall(this.selectedReport.name)
                    .then(({data}) => {
                        const parameterValues = mapParameterArrayToRecord(data.data);
                        const newReport = {
                            name: this.selectedReport.name,
                            params: parameterValues
                        };
                        const newReports = [
                            ...this.workflowMetadata.reports,
                            newReport
                        ];
                        this.updateWorkflowReports(newReports);
                        this.reportsValid.push(this.initialValidValue(newReport));
                        this.removeImportedFile()
                        this.selectedReport = null;
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
                this.removeImportedFile()
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
            validateWorkflow() {
                const formData = new FormData()
                formData.append("file", this.importedFile)
                const params = `?branch=${this.workflowMetadata.git_branch}&commit=${this.workflowMetadata.git_commit}`

                api.post(`/workflow/validate/${params}`,
                    formData,
                    {
                        headers: {
                            "Content-Type": "multipart/form-data"
                        }
                    })
                    .then(({data}) => {
                        this.updateWorkflowReports(data.data);
                        this.reportsValid = Array(data.data.length).fill(true);
                        this.validationErrors = [];
                        this.isImportedReports = true
                    })
                    .catch((error) => {
                        this.updateWorkflowReports([]);
                        this.reportsValid = [];
                        this.validationErrors = error.response.data?.errors || [];
                    });
            }
        }
    })
</script>