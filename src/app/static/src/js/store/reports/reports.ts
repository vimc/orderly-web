import {ReportWithDate, RunnerRootState} from "../../utils/types";
import {Module} from "vuex";
import {actions} from "./actions";
import {mutations} from "./mutations";

export interface ReportsState {
    availableReports: ReportWithDate[],
    availableReportsError: string
    selectedReport: ReportWithDate | null
}

export const initialReportState = (): ReportsState => {
    return {
        availableReports: [],
        availableReportsError: "",
        selectedReport: null
    }
}

const namespaced = true;

export const reports: Module<ReportsState, RunnerRootState> = {
    namespaced,
    state: initialReportState(),
    actions,
    mutations
}