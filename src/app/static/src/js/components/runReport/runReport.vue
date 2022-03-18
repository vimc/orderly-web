<template>
    <div>
        <h2>Run a report</h2>
        <form class="mt-3">
            <git-update-reports :report-metadata="metadata"
                                :initial-branches="initialGitBranches"
                                :show-all-reports="false"
                                @branchSelected="branchSelected"
                                @commitSelected="commitSelected"
                                @reportsUpdate="updateReports">
            </git-update-reports>
            <div v-if="showReports" id="report-form-group" class="form-group row">
                <label for="report" class="col-sm-2 col-form-label text-right">Report</label>
                <div class="col-sm-6">
                    <report-list id="report" :reports="reports" :selected-report.sync="selectedReport"/>
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
                <parameter-list id="params-component" :params="parameterValues"
                                @paramsChanged="getParameterValues"></parameter-list>
            </div>
            <change-log v-if="showChangelog"
                        :changelog-type-options="metadata.changelog_types"
                        :custom-style="childCustomStyle"
                        @changelog="handleChangelog">
            </change-log>
            <div v-if="showRunButton" id="run-form-group" class="form-group row">
                <div class="col-sm-2"></div>
                <div class="col-sm-6">
                    <button class="btn" type="submit" :disabled="disableRun" @click.prevent="runReport">
                        Run report
                    </button>
                    <div v-if="runningStatus" id="run-report-status" class="text-secondary mt-2">
                        {{ runningStatus }}
                        <a href="#" @click.prevent="$emit('changeTab')">View log</a>
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
    import {ChildCustomStyle, ReportWithDate, RunnerRootState, RunReportMetadataDependency} from "../../utils/types";
    import {mapState} from "vuex";

    interface Data {
        reports: ReportWithDate[]
        selectedBranch: string
        selectedCommitId: string
        selectedReport: ReportWithDate[]
        selectedInstances: Record<string, string>
        error: string
        defaultMessage: string
        runningStatus: string
        runningKey: string
        disableRun: boolean
        parameterValues: Record<string, string>[]
        changelog: object | null
        childCustomStyle: ChildCustomStyle
    }

    interface Methods {
        handleInstancesValue: (instances: Record<string, string>) => void
        handleChangelog: (changelog: Record<string, string>) => void
        getParameterValues: (values: Record<string, string>[], valid: boolean) => void
        branchSelected: (newBranch: string) => void
        commitSelected: (newCommit: string) => void
        updateReports: (newReports: object[]) => void
        setParameters: () => void
        runReport: () => void
        clearRun: () => void
    }

    interface Props {
        initialReportName: string
    }

    interface Computed {
        metadata: RunReportMetadataDependency | null
        initialGitBranches: string[]
        showReports: number
        showInstances: string
        showRunButton: boolean
        showParameters: number
        showChangelog: string[]
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "RunReport",
        components: {
            ErrorInfo,
            GitUpdateReports,
            ReportList,
            ParameterList,
            ChangeLog,
            Instances
        },
        props: {
            initialReportName: String
        },
        data: () => {
            return {
                reports: [],
                selectedBranch: "",
                selectedCommitId: "",
                selectedReport: null,
                selectedInstances: {},
                error: "",
                defaultMessage: "",
                runningStatus: "",
                runningKey: "",
                disableRun: false,
                parameterValues: [],
                changelog: null,
                childCustomStyle: {label: "col-sm-2 text-right", control: "col-sm-6"}
            }
        },
        computed: {
            ...mapState({
                initialGitBranches: (state: RunnerRootState) => state.git.branches,
                metadata: (state: RunnerRootState) => state.git.metadata
            }),
            showReports() {
                return this.reports && this.reports.length;
            },
            showInstances() {
                return !!this.selectedReport && this.metadata?.instances_supported;
            },
            showRunButton() {
                return !!this.selectedReport;
            },
            showParameters() {
                return !!this.selectedReport && this.parameterValues.length
            },
            showChangelog: function () {
                return !!this.selectedReport && this.metadata?.changelog_types
            }
        },
        watch: {
            metadata(val) {
                if (val && val.instances_supported) {
                    const instances = val.instances;
                    for (const key in instances) {
                        if (instances[key].length > 0) {
                            this.$set(this.selectedInstances, key, instances[key][0]);
                        }
                    }
                }
            },
            selectedReport: {
                deep: true,
                handler() {
                    this.clearRun();
                    if (this.selectedReport) {
                        this.setParameters();
                    }
                    this.parameterValues.length = 0
                }
            },
            selectedInstances: {
                deep: true,
                handler() {
                    this.clearRun()
                }
            }
        },
        mounted() {
            this.selectedReport = this.reports.find(report => report.name === this.initialReportName);
        },
        methods: {
            handleInstancesValue: function (instances) {
                this.selectedInstances = instances
            },
            handleChangelog: function (changelog) {
                this.changelog = changelog;
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
                this.selectedReport = this.reports.find(report => report.name === this.initialReportName);
            },
            setParameters: function () {
                const commit = this.selectedCommitId ? `?commit=${this.selectedCommitId}` : ''
                api.get(`/report/${this.selectedReport.name}/config/parameters/${commit}`)
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
                if (this.metadata && this.metadata.instances_supported && this.metadata.instances &&
                    Object.keys(this.metadata.instances).length > 0) {
                    const instDict = this.metadata.instances;
                    const instKeys = Object.keys(instDict);
                    const instanceName = instKeys.sort((a, b) => instDict[b].length - instDict[a].length)[0];
                    const instance = this.selectedInstances[instanceName];
                    instances = Object.keys(instDict).reduce((a, e) => ({[e]: instance, ...a}), {});
                }
                let params = {};
                params = this.parameterValues.reduce((params, param) => ({...params, [param.name]: param.value}), {});

                api.post(`/report/${this.selectedReport.name}/actions/run/`, {
                    instances,
                    params,
                    changelog: this.changelog,
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
                this.changelog = null;
            }
        }
    })
</script>
