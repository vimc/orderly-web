import Vue from "vue";
import Vuex, {StoreOptions} from "vuex";
import {git} from "../git/git";
import {RunnerRootState} from "../../utils/types";
import {mutations} from "./mutations";
import {reports} from "../reports/reports";

export type RunReportTabName = "RunReport" | "ReportLogs"

export interface RunReportRootState extends RunnerRootState {
    selectedTab: RunReportTabName
}

export const namespace = {
    git: "git",
    reports: "reports"
}

export const storeOptions: StoreOptions<RunReportRootState> = {
    state: {
        selectedTab: "RunReport"
    } as RunReportRootState,
    modules: {
        [namespace.git]: git,
        [namespace.reports]: reports,
    },
    mutations
};

Vue.use(Vuex);

export const store = new Vuex.Store<RunnerRootState>(storeOptions);
