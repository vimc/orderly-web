<template>
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
import Vue from "vue"
import {InfoIcon} from "vue-feather-icons";
import {Dependency, WorkflowReportWithDependency} from "../../utils/types";
import {VTooltip} from "v-tooltip";

interface Props {
    dependencies: Dependency;
}

interface Methods {
    paramSize: (report: WorkflowReportWithDependency) => number;
    showDetails: (params: Record<string, string>) => Record<string, string>;
    reportInfo: (reportName: string) => string;
}

export default Vue.extend<unknown, Methods, unknown, Props>({
    name: "reportParameter",
    props: {
        dependencies: {
            required: true,
            type: Object
        }
    },
    methods: {
        reportInfo(reportName) {
            const reportNum = this.dependencies.reports.filter(report => report.name === reportName).length
            return `${reportName} runs ${reportNum} times`;
        },
        paramSize(report) {
            return report.params? Object.keys(report.params).length : 0
        },
        showDetails(params) {
            return Object.keys(params).slice(3).reduce((remainingParams, key) => {
                remainingParams[key] = params[key]
                return remainingParams
            }, {});
        }
    },
    components: {
        InfoIcon
    },
    directives: {tooltip: VTooltip}

});
</script>