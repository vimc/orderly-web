import {MutationTree} from "vuex";
import {ReportsState, ReportWithDate} from "../../utils/types";

export enum ReportsMutation {
    SetRunnableReports = "SetRunnableReports"
}

export const mutations: MutationTree<ReportsState> = {
    [ReportsMutation.SetRunnableReports](state: ReportsState, payload: ReportWithDate[]) {
        state.runnableReports = payload;
    }
}
