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
                            <!--Dependencies boxes should go here, you might want to consider using slots or
                             perhaps add html here directly instead of creating another component -->
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
    import {WorkflowSummaryResponse} from "../../../utils/types";
    import {VTooltip} from "v-tooltip";
    import runWorkflowParameters from "./../runWorkflowParameters.vue"

    interface Props {
        workflowSummary: WorkflowSummaryResponse
        gitCommit: string
    }

    interface Methods {
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
            },
            gitCommit: {
                required: true,
                type: String
            },
        },
        methods: {
            reportInfo(reportName) {
                const reportNum = this.workflowSummary.reports.filter(report => report.name === reportName).length
                return `${reportName} runs ${reportNum} ${reportNum <= 1 ? 'time' : 'times'}`;
            },
        }
    });
</script>
