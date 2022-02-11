<template>
    <div id="workflow-summary" class="mt-3">
        <div class="row">
            <div class="col-12">
                <div class="summary">
                    <div v-for="(report, index) in workflowSummary.reports" :key="index" class="single-workflow-summary-area">
                        <div class="workflow-summary-report">
                        </div>
                        <div id="report-name-icon" class="d-inline-block">
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
                            <div class="report-params col-12 col-md-6 col-lg-4">
                                <div class="single-workflow-summary-content parameters-bg-color d-flex">
                                    <div class="workflow-summary-text">
                                        <span class="text-muted d-inline-block">Parameters</span>
                                        <div v-if="hasParams(report)">
                                            <p class="non-default-param"
                                               v-for="param in nonDefaultParams(index)"
                                               :key="param.name">{{ param.name }}: {{ param.value }}</p>
                                            <div v-if="defaultParams(index).length" :id="`default-params-${index}`">
                                                <a :href="`#collapseSummary-${index}`"
                                                   class="show-defaults pt-2 d-inline-block small"
                                                   data-toggle="collapse"
                                                   aria-expanded="false"
                                                   aria-controls="collapseSummary">
                                                    Show defaults...
                                                </a>
                                                <div  :id="`collapseSummary-${index}`" class="collapse">
                                                    <p :id="`default-params-collapse-${key}`"
                                                       v-for="param in defaultParams(index)"
                                                       :key="key">{{ param.name }}: {{ param.value }}</p>
                                                </div>
                                            </div>
                                            <error-info v-if="getDefaultParamsError(report.name)"
                                                        :default-message="defaultMessage"
                                                        :api-error="getDefaultParamsError(report.name)">
                                            </error-info>
                                        </div>
                                        <div v-else><p>There are no parameters</p></div>
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
import {WorkflowSummary, WorkflowReportWithDependencies, Parameter} from "../../../utils/types";
import {VTooltip} from "v-tooltip";
import runWorkflowMixin from "../runWorkflowMixin.ts";
import ErrorInfo from "../../errorInfo.vue";

interface Props {
    workflowSummary: WorkflowSummary;
    gitCommit: string;
}

interface Methods {
    hasParams: (report: WorkflowReportWithDependencies) => boolean;
    reportInfo: (reportName: string) => string;
    nonDefaultParams: (idx: number) => Parameter[]
    defaultParams: (idx: number) => Parameter[]
    getDefaultParamsError: (reportName: string) => string
}

interface Data {
    defaultMessage: string
}

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "workflowSummaryReports",
        data() {
            return {
                defaultMessage: "An error occurred while retrieving default params"
            }
        },
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
            return report.params ? !!Object.keys(report.params).length : false
        },
        nonDefaultParams(idx) {
            return this.workflowReportParams ? this.workflowReportParams[idx].nonDefaultParams : [];
        },
        defaultParams(idx) {
            return this.workflowReportParams ? this.workflowReportParams[idx].defaultParams : [];
        },
        getDefaultParamsError(reportName) {
            return this.defaultParamsErrors?.find(error => error.reportName === reportName) || "";
        }
    },
    mixins: [runWorkflowMixin],
    beforeMount() {
        this.getWorkflowReportParams(this.workflowSummary, this.gitCommit);
    },
    components: {
        InfoIcon,
        ErrorInfo
    },
    directives: {tooltip: VTooltip}
});
</script>
