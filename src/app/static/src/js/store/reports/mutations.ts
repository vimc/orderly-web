import {MutationTree} from "vuex";
import {ReportsState} from "./reports";
import {PayloadWithType, ReportWithDate, Error} from "../../utils/types";
import {reportComparator} from "../../utils/helpers";

export enum ReportsMutation {
    FetchReports = "FetchReports",
    FetchReportsError = "FetchReportsError",
    SelectReport = "SelectReport"
}

export const mutations: MutationTree<ReportsState> = {

    [ReportsMutation.FetchReports](state: ReportsState, action: PayloadWithType<ReportWithDate[]>) {
        state.reports = [...action.payload].sort(reportComparator)
    },

    [ReportsMutation.FetchReportsError](state: ReportsState, action: PayloadWithType<Error>) {
        state.reportsError = action.payload
    },

    [ReportsMutation.SelectReport](state: ReportsState, payload: ReportWithDate | null) {
        state.selectedReport = payload
    }
}