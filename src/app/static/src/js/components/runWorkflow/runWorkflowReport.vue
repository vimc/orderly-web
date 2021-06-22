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
                <b-alert
                    :show="!!workflowRemovals"
                    dismissible
                    variant="warning"
                    @dismissed="workflowRemovals=null">
                    The following items are not present in this git commit and have been removed from the workflow:
                    <ul class="py-0 my-0 ml-2" :style="{listStyleType: 'disc'}">
                        <li v-for="item in workflowRemovals">{{item}}</li>
                    </ul>
                </b-alert>
            </div>
            <div class="pb-4" id="workflow-reports">
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
                            @paramsChanged="(...args) => paramsChanged(index, ...args)"
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
    stepIsValid: boolean
}

interface Methods {
    branchSelected: (git_branch: string) => void,
    commitSelected: (git_commit: string) => void,
    getParametersApiCall: (report: string) => Promise<AxiosResponse<any>>,
    updateAvailableReports: (reports: ReportWithDate[]) =>  void,
    addReport: () => void
    paramsChanged: (index: number, params: Parameter[], valid: boolean) => void
    removeReport: (index: number) => void
    updateWorkflowReports: (reports: WorkflowReportWithParams[]) => void
    initialValidValue: (report: WorkflowReportWithParams) => boolean
}

interface Data {
    runReportMetadata: RunReportMetadata | null,
    initialBranches:  string[] | null,
    reports: ReportWithDate[],
    selectedReport: string,
    error: string,
    defaultMessage: string,
    workflowRemovals: string[] | null,
    reportsValid: boolean[]
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
            defaultMessage: "",
            workflowRemovals: null,
            reportsValid: []
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
        },
        stepIsValid: function() {
            return (this.reportsValid.length > 0) && (this.reportsValid.every(v => v));
        }
    },
    methods: {
        branchSelected(git_branch: string) {
            this.$emit("update", {git_branch});
        },
        commitSelected(git_commit: string) {
            this.$emit("update", {git_commit})
        },
        getParametersApiCall(report: string) {
            const commit = this.workflowMetadata.git_commit ? `?commit=${this.workflowMetadata.git_commit}` : '';
            return api.get(`/report/${report}/parameters/${commit}`);
        },
        async updateAvailableReports(reports: ReportWithDate[]) {
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
            const availableReportNames = reports.map(r => r.name);
            const validityIndexRemovals = [];
            this.workflowMetadata.reports.forEach((r: WorkflowReportWithParams, index: number) => {
                if (availableReportNames.includes(r.name)) {
                    newReports.push({...r, params: {...r.params}});
                } else {
                    addToRemovals(`Report '${r.name}'`);
                    validityIndexRemovals.push(index);
                }
            });

            //Deal with removing valid flags by index in reverse to avoid moving index bug
            for(let i = validityIndexRemovals.length-1; i >=0; i--) {
                this.reportsValid.splice(validityIndexRemovals[i], 1);
            }

            // 2. Check parameters
            const calls = newReports.map((r, index) => {
                return this.getParametersApiCall(r.name)
                    .then(({data}) => {
                        const newParameterValues = mapParameterArrayToRecord(data.data);
                        // Check for parameters in metadata not in fetched params
                        for (const p of Object.keys(r.params)) {
                            if (!Object.keys(newParameterValues).includes(p)) {
                                delete r.params[p];
                                addToRemovals(`Parameter '${p}' in report '${r.name}'`);

                                // If we have removed the last param from a report it becomes valid
                                if (Object.keys(r.params).length === 0) {
                                    this.$set(this.reportsValid, index, true);
                                }
                            }
                        }

                        // Check for parameters in fetched params not in metadata
                        for (const p of Object.keys(newParameterValues)) {
                            if (!Object.keys(r.params).includes(p)) {
                                r.params[p] = newParameterValues[p];
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
                const newReportNames = reports.map(r => r.name);
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

        this.reportsValid = this.workflowMetadata.reports.map(r => this.initialValidValue(r));
    },
    watch: {
        stepIsValid(newVal) {
            this.$emit("valid", newVal);
        }
    }
})
</script>
