import Vue from "vue";
import {api} from "../../utils/api";
import {WorkflowSummary, Parameter} from "../../utils/types";
import {AxiosResponse} from "axios";

interface Data {
    defaultParams: Record<string, Parameter[]>[];
    defaultParamsErrors: [];
}

interface Methods {
    getParametersApiCall: (reportName: string, gitCommit: string) => Promise<AxiosResponse>;
    getDefaultParameters: (workflowSummary: WorkflowSummary, gitCommit: string) => void;
}

export default Vue.extend<Data, Methods, unknown, unknown>({
    data() {
        return {
            defaultParams: [],
            defaultParamsErrors: []
        }
    },
    methods: {
        getParametersApiCall(reportName, gitCommit) {
            const commit = gitCommit ? `?commit=${gitCommit}` : '';
            return api.get(`/report/${reportName}/config/parameters/${commit}`);
        },
        getDefaultParameters(workflowSummary, gitCommit) {
            workflowSummary?.reports.map(report => {
                this.getParametersApiCall(report.name, gitCommit)
                    .then(({data}) => {
                        this.defaultParams.push({reportName: report.name, params: data.data});
                    })
                    .catch((error) => {
                        this.defaultParamsErrors.push({reportName: report.name, error: error});
                    })
            })
        }
    }
})