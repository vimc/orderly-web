import Vue from "vue";
import {api} from "../../utils/api";
import {WorkflowSummaryResponse, Parameter} from "../../utils/types";
import {AxiosResponse} from "axios";

interface WorkflowReportParams {
    defaultParams: Parameter[],
    nonDefaultParams: Parameter[]
}

interface Data {
    workflowReportParams: WorkflowReportParams[] | null;
    defaultParamsErrors: Record<string, Error>
}

interface Methods {
    getParametersApiCall: (reportName: string, gitCommit: string) => Promise<AxiosResponse>;
    getWorkflowReportParams: (workflowSummary: WorkflowSummaryResponse, gitCommit: string) => void
}

export default Vue.extend<Data, Methods, unknown, unknown>({
    data() {
        return {
            workflowReportParams: null,
            defaultParamsErrors: {}
        }
    },
    methods: {
        getParametersApiCall(reportName, gitCommit) {
            const commit = gitCommit ? `?commit=${gitCommit}` : '';
            return api.get(`/report/${reportName}/config/parameters/${commit}`);
        },
        // For each report in workflowSummary, map to an object which contains both defaultParams and nonDefaultParams
        getWorkflowReportParams: async function (workflowSummary: WorkflowSummaryResponse, gitCommit: string) {
            const workflowReportParams = [];
            const defaultParamsPerReport: Record<string, Parameter[]> = {};

            for (let report of workflowSummary.reports) {
                //1. Fetch default params for this report if we don't already have them
                if (!Object.keys(defaultParamsPerReport).includes(report.name)) {
                    // Only fetch if  report has any parameters values.
                    if (Object.keys(report.params).length === 0) {
                        defaultParamsPerReport[report.name] = [];
                    } else {
                        await this.getParametersApiCall(report.name, gitCommit)
                            .then(({data}) => {
                                defaultParamsPerReport[report.name] = data.data;
                            })
                            .catch((error) => {
                                this.defaultParamsErrors[report.name] = error;
                                //Push empty to defaults - couldn't retrieve - all parameters will show as non-default
                                defaultParamsPerReport[report.name] = [];
                            })
                    }
                }

                //2. Sort report params into default and non-default
                const defaultParams = defaultParamsPerReport[report.name];
                const reportParams: WorkflowReportParams = {
                    defaultParams: [],
                    nonDefaultParams: []
                };
                Object.keys(report.params).forEach(paramName => {
                    const defaultValue = defaultParams.find(defaultParam => defaultParam.name === paramName)?.value;
                    const param = {name: paramName, value: report.params[paramName]};
                    if (defaultValue === param.value) {
                        reportParams.defaultParams.push(param);
                    } else {
                        reportParams.nonDefaultParams.push(param);
                    }
                });
                workflowReportParams.push(reportParams);
            }
            this.workflowReportParams = workflowReportParams;
        }
    }
})