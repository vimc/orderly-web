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
                                        <div v-if="hasParams(report)">
                                            <p v-for="param in report.param_list"
                                               :key="param.name"
                                               class="non-default-param">{{ param.name }}: {{ param.value }}</p>
                                            <div v-if="report.default_param_list.length > 0"
                                                 :id="`default-params-${index}`">
                                                <b-link v-b-toggle="`collapseSummary-${index}`"
                                                        href="#"
                                                        class="show-defaults pt-2 d-inline-block small">
                                                    <span class="when-closed">Show</span>
                                                    <span class="when-open">Hide</span> defaults...
                                                </b-link>
                                                <b-collapse :id="`collapseSummary-${index}`">
                                                    <p v-for="(param, paramIndex) in report.default_param_list"
                                                       :id="`default-params-collapse-${index}-${paramIndex}`"
                                                       :key="param.name">{{ param.name }}: {{ param.value }}</p>
                                                </b-collapse>
                                            </div>
                                        </div>
                                        <div v-else>
                                            <p>There are no parameters</p>
                                        </div>
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
    import {BLink} from "bootstrap-vue/esm/components/link";
    import {BCollapse} from "bootstrap-vue/esm/components/collapse";
    import {VBToggle} from 'bootstrap-vue/esm/directives/toggle';
    import {WorkflowSummary, WorkflowReportWithDependencies} from "../../../utils/types";
    import {VTooltip} from "v-tooltip";

    interface Props {
        workflowSummary: WorkflowSummary
        gitCommit: string
    }

    interface Methods {
        hasParams: (report: WorkflowReportWithDependencies) => boolean
        reportInfo: (reportName: string) => string
    }

    Vue.directive("b-toggle", VBToggle);

    export default Vue.extend<unknown, Methods, unknown, Props>({
        name: "WorkflowSummaryReports",
        components: {
            BCollapse,
            BLink,
            InfoIcon
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
            hasParams(report) {
                return (report.param_list && report.param_list.length > 0) ||
                    (report.default_param_list && report.default_param_list.length > 0)
            }
        }
    });
</script>
