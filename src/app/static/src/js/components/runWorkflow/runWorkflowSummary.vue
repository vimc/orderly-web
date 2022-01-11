<template>
    <div>
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
        <div id="summary-timeline" class="mt-3">
            <div class="row">
                <div class="col-12">
                    <div class="summary">
                        <div v-for="report in workflowMetadata.reports" class="single-timeline-area">
                            <div class="timeline-date wow fadeInLeft" data-wow-delay="0.1s"
                                 style="visibility: visible; animation-delay: 0.1s; animation-name: fadeInLeft;">
                            </div>
                            <div class="d-inline-block" v-tooltip="reportInfo(report)">
                                <h5>{{ report.name }}
                                    <span>
                                        <info-icon size="1.2x" stroke="grey" class="custom-class"/>
                                    </span>
                                </h5>
                            </div>
                            <div class="row">
                                <div class="col-12 col-md-6 col-lg-4">
                                    <div class="single-timeline-content parameters-bg-color d-flex wow fadeInLeft"
                                         data-wow-delay="0.3s"
                                         style="visibility: visible; animation-delay: 0.3s; animation-name: fadeInLeft;">
                                        <div class="timeline-text">
                                            <span class="text-muted d-inline-block">Parameters</span>
                                            <div v-if="!!paramSize(report)">
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
                                <div class="col-12 col-md-6 col-lg-4" v-if="hasDependencies(report.name)">
                                    <div class="single-timeline-content d-flex wow fadeInLeft" data-wow-delay="0.5s"
                                         style="visibility: visible; animation-delay: 0.5s; animation-name: fadeInLeft;">
                                        <div class="timeline-text">
                                            <div class="pb-2" v-if="reportDependsOn(report.name).length">
                                                <span class="text-muted d-inline-block">Depends on</span>
                                                <div v-for="report in reportDependsOn(report.name)">
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
    import {RunWorkflowMetadata, WorkflowReportWithParams, Dependency} from "../../utils/types";
    import {AlertTriangleIcon, InfoIcon} from "vue-feather-icons"
    import {api} from "../../utils/api";
    import {VTooltip} from 'v-tooltip';

    interface Props {
        workflowMetadata: RunWorkflowMetadata;
    }

    interface Computed {
        hasMissingDependencies: boolean;
    }

    interface Methods {
        paramSize: (report: WorkflowReportWithParams) => number;
        showDetails: (params: Record<string, string>) => Record<string, string>;
        getReportDependencies: () => void;
        missingDependencies: (reportName: string) => string[]
        reportInfo: (report: WorkflowReportWithParams) => string;
        reportDependsOn: (reportName: string) => string[];
        hasDependencies: (reportName: string) => boolean
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
                return this.dependencies && !!Object.keys(this.dependencies.missing_dependencies)
                    .some(reportName => this.dependencies.missing_dependencies[reportName].length > 0);
            }
        },
        methods: {
            reportInfo(report) {
                return `'${report.name}' runs ${(Object.keys(report.params).length)} times`
            },
            paramSize(report) {
                return Object.keys(report.params).length;
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
            reportDependsOn(reportName) {
                return this.dependencies?.reports.find(report => report.name === reportName)!.depends_on || [];
            },
            hasDependencies(reportName) {
                return this.missingDependencies(reportName).length || this.reportDependsOn(reportName).length
            }
        },
        mounted() {
            this.getReportDependencies();
            this.$emit("valid", true)
        },
        directives: {tooltip: VTooltip},
    })
</script>
