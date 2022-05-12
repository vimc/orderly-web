import {MutationTree} from "vuex";
import {ReportsState} from "./reports";
import {PayloadWithType, ReportWithDate} from "../../utils/types";

export enum ReportsMutation {
    AvailableReport = "SelectedReport",
    AvailableReportError = "ReportError",
    SelectReport = "SelectReport"
}

export const mutations: MutationTree<ReportsState> = {

    [ReportsMutation.AvailableReport](state: ReportsState, action: PayloadWithType<ReportWithDate[]>) {
        state.availableReports = [...action.payload].sort((a, b) => a.name.localeCompare(b.name))
    },

    [ReportsMutation.AvailableReportError](state: ReportsState, action: PayloadWithType<string>) {
        state.availableReportsError = action.payload
    },

    [ReportsMutation.SelectReport](state: ReportsState, payload: ReportWithDate | null) {
        state.selectedReport = payload
    }
}