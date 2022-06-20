import {MutationTree} from "vuex";
import {ReportsState} from "./reports";
import {PayloadWithType, ReportWithDate, Error} from "../../utils/types";
import {reportComparator} from "../../utils/helpers";

export enum ReportsMutation {
    SetReports = "SetReports",
    SetReportsError = "SetReportsError",
    SelectReport = "SelectReport"
}

export const mutations: MutationTree<ReportsState> = {

    [ReportsMutation.SetReports](state: ReportsState, action: PayloadWithType<ReportWithDate[]>) {
        state.reports = [...action.payload].sort(reportComparator)

        if (state.reports.length) {
            state.selectedReport = state.reports[0]
        }
    },

    [ReportsMutation.SetReportsError](state: ReportsState, action: PayloadWithType<Error>) {
        state.reportsError = action.payload
    },

    [ReportsMutation.SelectReport](state: ReportsState, payload: ReportWithDate) {
        state.selectedReport = payload
    }
}