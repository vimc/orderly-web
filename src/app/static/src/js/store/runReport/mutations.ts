import {MutationTree} from "vuex";
import {RunReportRootState, RunReportTabName} from "./store";

export enum RunReportMutation {
    SwitchTab = "SwitchTab"
}

export const mutations: MutationTree<RunReportRootState> = {
    [RunReportMutation.SwitchTab](state: RunReportRootState, payload: RunReportTabName) {
        state.selectedTab = payload
    }
}
