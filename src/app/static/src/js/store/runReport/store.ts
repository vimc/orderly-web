import Vue from "vue";
import Vuex, {StoreOptions} from "vuex";
import {git} from "../git/git";
import {RunnerRootState} from "../../utils/types";
import {mutations} from "./mutations";

export type RunReportTabName = "RunReport" | "ReportLogs"

export interface RunReportRootState extends RunnerRootState {
    selectedTab: RunReportTabName
}

export const storeOptions: StoreOptions<RunReportRootState> = {
    state: {
        selectedTab: "RunReport"
    } as RunReportRootState,
    modules: {
        git
    },
    mutations
};

Vue.use(Vuex);

export const store = new Vuex.Store<RunReportRootState>(storeOptions);
