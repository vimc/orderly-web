<template>
    <div id="workflow-summary" class="mt-3">
        <div class="row">
            <div class="col-12">
                <div class="summary">
                    <div v-for="(report, index) in dependencies.reports" class="single-workflow-summary-area">
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
                                            <p id="params" v-for="(value, key) in report.params">{{ key }}: {{ value }}</p>
                                            <div id="default-params" v-if="showDefaultParameters(report.name)" :key="index">
                                                <a href="#collapseSummary"
                                                   class="pt-2 d-inline-block small"
                                                   data-toggle="collapse"
                                                   aria-expanded="false"
                                                   aria-controls="collapseSummary"
                                                >Show default...</a>
                                                <div id="collapseSummary" class="collapse">
                                                    <p id="default-params-collapse"v-for="(param, index) in showDefaultParameters(report.name)">{{ param.name }}: {{ param.value }}</p>
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
import {Dependency, Error, Parameter, WorkflowReportWithDependency} from "../../../utils/types";
import {VTooltip} from "v-tooltip";
import {api} from "../../../utils/api";
import {AxiosResponse} from "axios";

interface Props {
    dependencies: Dependency;
    gitCommit: string;
}

interface Methods {
    paramSize: (report: WorkflowReportWithDependency) => number;
    reportInfo: (reportName: string) => string;
    getParametersApiCall: (reportName: string) => Promise<AxiosResponse>
    getDefaultParameters: () => void
    showDefaultParameters: (reportName: string) => Parameter | null;
}

interface Data {
    defaultParams: Record<string, Parameter[]>[]
    defaultParamsError: Error[],
}

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "reportParameter",
    props: {
        dependencies: {
            required: true,
            type: Object
        },
        gitCommit: {
            required: true,
            type: String
        }
    },
    data() {
        return {
            defaultParams: [],
            defaultParamsError: []
        }
    },
    methods: {
        reportInfo(reportName) {
            const reportNum = this.dependencies.reports.filter(report => report.name === reportName).length
            return `${reportName} runs ${reportNum} times`;
        },
        paramSize(report) {
            return report.params ? Object.keys(report.params).length : 0
        },
        getParametersApiCall(reportName) {
            const commit = this.gitCommit ? `?commit=${this.gitCommit}` : '';
            return api.get(`/report/${reportName}/config/parameters/${commit}`)
        },
        getDefaultParameters() {
            this.dependencies?.reports.map(report => {
                this.getParametersApiCall(report.name)
                    .then(({data}) => {
                        this.defaultParams.push({reportName: report.name, params: data.data})
                    })
                    .catch((error) => {
                        this.defaultParamsError.push({reportName: report.name, error: error})
                    })
            })
        },
        showDefaultParameters(reportName) {
            return this.defaultParams?.find(data => data.reportName === reportName)?.params || null
        }
    },
    mounted() {
        this.getDefaultParameters()
    },
    components: {
        InfoIcon
    },
    directives: {tooltip: VTooltip}

});
</script>