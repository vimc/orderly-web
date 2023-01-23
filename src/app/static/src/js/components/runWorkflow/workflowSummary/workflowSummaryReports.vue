<template>
    <div id="workflow-summary" class="mt-3">
        <div class="row">
            <div class="col-12">
                <div class="summary">
                    <div v-for="(report, index) in workflowSummary.reports" :key="index"
                         class="single-workflow-summary-area">
                        <div class="workflow-summary-report">
                        </div>
                        <div id="report-name-icon" class="d-inline-block">
                            <h5>{{ report.name }}
                                <span>
                                    <info-icon v-tooltip="reportInfo(report.name)"
                                               size="1.2x"
                                               stroke="grey"
                                               class="custom-class"/>
                                </span>
                            </h5>
                        </div>
                        <div class="row">
                            <div class="report-params col-12 col-md-6 col-lg-4">
                                <div class="single-workflow-summary-content parameters-bg-color d-flex">
                                    <div class="workflow-summary-text">
                                        <span class="text-muted d-inline-block">Parameters</span>
                                        <run-workflow-parameters :report="report"
                                                                 :report-index="index"></run-workflow-parameters>
                                    </div>
                                </div>
                            </div>
                            <span class="d-inline-block"></span>
                            <div v-if="report.depends_on || hasMissingDependencies(report)" class="col-12 col-md-6 col-lg-4">
                                <div class="single-workflow-summary-content dependencies">
                                    <div class="workflow-summary-text">
                                        <div v-if="report.depends_on" class="dependsOn">
                                            <span class="text-muted m-0">Depends on</span>
                                            <p v-for="dependency in report.depends_on" :key="dependency">{{ dependency }}</p>
                                        </div>
                                        <div v-if="hasMissingDependencies(report)" class="missingDependency">
                                            <span class="text-danger m-0">Missing dependency</span>
                                            <p v-for="missingDependency in workflowSummary.missing_dependencies[report.name]" :key="missingDependency">{{ missingDependency }}</p>
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
</template>

<script lang="ts">
    import Vue from "vue";
    import {InfoIcon} from "vue-feather-icons";
    import {WorkflowSummaryResponse, WorkflowReportWithDependencies} from "../../../utils/types";
    import {VTooltip} from "v-tooltip";
    import runWorkflowParameters from "./../runWorkflowParameters.vue"

    interface Props {
        workflowSummary: WorkflowSummaryResponse
    }

    interface Methods {
        hasMissingDependencies: (report: WorkflowReportWithDependencies) => boolean
        reportInfo: (reportName: string) => string
    }

    export default Vue.extend<unknown, Methods, unknown, Props>({
        name: "WorkflowSummaryReports",
        components: {
            InfoIcon,
            runWorkflowParameters
        },
        directives: {tooltip: VTooltip},
        props: {
            workflowSummary: {
                required: true,
                type: Object
            }
        },
        methods: {
            reportInfo(reportName) {
                const reportNum = this.workflowSummary.reports.filter(report => report.name === reportName).length
                return `${reportName} runs ${reportNum} ${reportNum <= 1 ? 'time' : 'times'}`;
            },
            hasMissingDependencies(report) {
                return report.name && this.workflowSummary?.missing_dependencies && this.workflowSummary.missing_dependencies[report.name]?.length
            }
        }
    });
</script>
