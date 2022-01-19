<template>
    <div id="workflow-summary" class="mt-3">
        <div class="row">
            <div class="col-12">
                <div class="summary">
                    <div v-for="(report, index) in workflowSummary.reports" :key="index" class="single-workflow-summary-area">
                        <div class="workflow-summary-report wow fadeInLeft">
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
                            <div id="report-params" class="col-12 col-md-6 col-lg-4">
                                <div class="single-workflow-summary-content parameters-bg-color d-flex wow fadeInLeft">
                                    <div class="workflow-summary-text">
                                        <span class="text-muted d-inline-block">Parameters</span>
                                        <div v-if="paramSize(report)">
                                            <p id="params"
                                               v-for="(value, key, index) in report.params"
                                               :key="index">{{ key }}: {{ value }}</p>
                                            <div :id="`default-params-${index}`" >
                                                <a href="#collapseSummary"
                                                   class="pt-2 d-inline-block small"
                                                   data-toggle="collapse"
                                                   aria-expanded="false"
                                                   aria-controls="collapseSummary"
                                                >Show default...</a>
                                                <div id="collapseSummary" class="collapse">
                                                    <p id="default-params-collapse"
                                                       v-for="(param, index) in showDefaultParameters(report.name)"
                                                       :key="index">{{ param.name }}: {{ param.value }}</p>
                                                    <error-info v-if="getDefaultParametersError(report.name)"
                                                                :default-message="defaultMessage"
                                                                :api-error="getDefaultParametersError(report.name)">
                                                    </error-info>
                                                </div>
                                            </div>
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
import {WorkflowSummary, Parameter, WorkflowReportWithDependency} from "../../../utils/types";
import {VTooltip} from "v-tooltip";
import runWorkflowMixin from "../runWorkflowMixin.ts";
import ErrorInfo from "../../errorInfo.vue";

interface Props {
    workflowSummary: WorkflowSummary;
    gitCommit: string;
}

interface Methods {
    paramSize: (report: WorkflowReportWithDependency) => number;
    reportInfo: (reportName: string) => string;
    showDefaultParameters: (reportName: string) => Parameter | null;
    getDefaultParametersError: (reportName: string) => string
}

interface Data {
    defaultMessage: string
}

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "reportParameter",
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
        }
    },
    methods: {
        reportInfo(reportName) {
            const reportNum = this.workflowSummary.reports.filter(report => report.name === reportName).length
            return `${reportName} runs ${reportNum} times`;
        },
        paramSize(report) {
            return report.params ? Object.keys(report.params).length : 0
        },
        showDefaultParameters(reportName) {
            return this.defaultParams?.find(data => data.reportName === reportName)?.params || null
        },
        getDefaultParametersError(reportName) {
            return this.defaultParamsErrors?.find(error => error.reportName === reportName) || ""
        }
    },
    mixins: [runWorkflowMixin],
    mounted() {
        this.getDefaultParameters(this.workflowSummary, this.gitCommit)
    },
    components: {
        InfoIcon,
        ErrorInfo
    },
    directives: {tooltip: VTooltip}

});
</script>