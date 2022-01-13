<template>
    <div v-if="hasDependenciesLength">
        <h2 id="summary-header">Summary</h2>
        <div id="summary-warning" class="row mt-3" v-if="hasMissingDependencies">
            <div class="col-auto">
                <alert-triangle-icon size="2x" stroke="red" class="custom-class"/>
            </div>
            <div class="col-auto">
                <span class="d-inline-block pb-2"> Some reports depend on the latest version of other reports that are not included in your workflow:</span>
                <div v-for="(missing_dependencies, report) in dependencies.missing_dependencies">
                    <span v-if="missing_dependencies.length" class="font-weight-bold"> {{ report }}</span>
                    <ul v-for="missing_dependency in missing_dependencies" class="styled">
                        <li>{{ missing_dependency }}</li>
                    </ul>
                </div>
            </div>
            <hr>
        </div>
        <div id="workflow-summary" class="mt-3">
            <div class="row">
                <div class="col-12">
                    <div class="summary">
                        <div v-for="report in dependencies.reports" class="single-workflow-summary-area">
                            <div class="workflow-summary-report wow fadeInLeft">
                            </div>
                            <div class="d-inline-block">
                                <h5>{{ report.name }}
                                    <span>
                                        <info-icon size="1.2x"
                                                   stroke="grey"
                                                   v-tooltip="reportInfo(report.name)"
                                                   class="custom-class"/>
                                    </span>
                                </h5>
                            </div>
                            <div class="row">
                                <div class="col-12 col-md-6 col-lg-4">
                                    <div class="single-workflow-summary-content parameters-bg-color d-flex wow fadeInLeft">
                                        <div class="workflow-summary-text">
                                            <span class="text-muted d-inline-block">Parameters</span>
                                            <div v-if="paramSize(report)">
                                                <p v-for="(value, key) in report.params">{{ key }}: {{ value }}</p>
                                                <div v-if="paramSize(report) > 3">
                                                    <a href="#collapseSummary"
                                                       class="pt-2 d-inline-block small"
                                                       data-toggle="collapse"
                                                       aria-expanded="false"
                                                       aria-controls="collapseSummary"
                                                    >Show default...</a>
                                                    <div class="collapse" id="collapseSummary">
                                                        <p v-for="(value, key) in showDetails(report.params)">{{ key }}:
                                                            {{ value }}</p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div v-else><p>There are no parameters</p></div>
                                        </div>
                                    </div>
                                </div>
                                <span class="d-inline-block"></span>
                                <div class="col-12 col-md-6 col-lg-4" v-if="showDependencies(report)">
                                    <div class="single-workflow-summary-content d-flex wow fadeInLeft">
                                        <div class="workflow-summary-text">
                                            <div class="pb-2" v-if="reportDependsOn(report).length">
                                                <span class="text-muted d-inline-block">Depends on</span>
                                                <div v-for="report in reportDependsOn(report)">
                                                    <p>{{ report }}</p>
                                                </div>
                                            </div>
                                            <div class="text-danger" v-if="missingDependencies(report.name).length">
                                                <span class="d-inline-block">Missing dependencies</span>
                                                <div v-for="dependency in missingDependencies(report.name)">
                                                    <p>{{ dependency }}</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="error"><p class="row mt-3 justify-content-center col-8 text-danger">{{ error }}</p></div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {
        RunWorkflowMetadata,
        Dependency,
        WorkflowReportWithDependency
    } from "../../utils/types";
    import {AlertTriangleIcon, InfoIcon} from "vue-feather-icons"
    import {api} from "../../utils/api";
    import {VTooltip} from 'v-tooltip';

    interface Props {
        workflowMetadata: RunWorkflowMetadata;
    }

    interface Computed {
        hasMissingDependencies: boolean;
        hasDependenciesLength: boolean;
    }

    interface Methods {
        paramSize: (report: WorkflowReportWithDependency) => number;
        showDetails: (params: Record<string, string>) => Record<string, string>;
        getReportDependencies: () => void;
        missingDependencies: (reportName: string) => string[]
        reportInfo: (reportName: string) => string;
        reportDependsOn: (report: WorkflowReportWithDependency) => string[];
        showDependencies: (report: WorkflowReportWithDependency) => boolean
    }

    interface Data {
        dependencies: Dependency | null
        error: string
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runWorkflowSummary",
        components: {
            AlertTriangleIcon,
            InfoIcon
        },
        data() {
            return {
                dependencies: null,
                error: ""
            }
        },
        props: {
            workflowMetadata: {
                required: true,
                type: Object
            }
        },
        computed: {
            hasMissingDependencies() {
                return !!Object.keys(this.dependencies.missing_dependencies)
                    .some(reportName => this.dependencies.missing_dependencies[reportName].length > 0);
            },
            hasDependenciesLength() {
                return !!this.dependencies
            }
        },
        methods: {
            reportInfo(reportName) {
                const reportNum = this.workflowMetadata?.reports.filter(report => report.name === reportName).length
                return `${reportName} runs ${reportNum} times`;
            },
            paramSize(report) {
                return report.params? Object.keys(report.params).length : 0
            },
            showDetails(params) {
                return Object.keys(params).slice(3).reduce((entireParams, key) => {
                    entireParams[key] = params[key]
                    return entireParams
                }, {});
            },
            getReportDependencies() {
                api.post(`/workflows/summary`, {
                    reports: this.workflowMetadata.reports,
                    ref: this.workflowMetadata.git_commit
                })
                    .then(({data}) => {
                        this.dependencies = data.data;
                        this.error = "";
                    })
                    .catch((error) => {
                        this.error = error;
                    })
            },
            missingDependencies(reportName) {
                return this.dependencies?.missing_dependencies[reportName] || [];
            },
            reportDependsOn(report) {
                return report!.depends_on || [];
            },
            showDependencies(report) {
                return this.missingDependencies(report.name).length || this.reportDependsOn(report).length
            }
        },
        mounted() {
            this.getReportDependencies();
            this.$emit("valid", true)
        },
        directives: {tooltip: VTooltip},
    })
</script>
