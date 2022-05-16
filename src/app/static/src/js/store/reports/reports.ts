import {ReportWithDate, RunnerRootState, Error} from "../../utils/types";
import {Module} from "vuex";
import {actions} from "./actions";
import {mutations} from "./mutations";

export interface ReportsState {
    reports: ReportWithDate[],
    reportsError: Error | null,
    selectedReport: ReportWithDate | null
}

export const initialReportState = (): ReportsState => {
    return {
        reports: [],
        reportsError: null,
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