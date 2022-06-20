import {ActionContext, ActionTree} from "vuex";
import {RunnerRootState} from "../../utils/types";
import {ReportsState} from "./reports";
import {api} from "../../utils/apiService";
import {ReportsMutation} from "./mutations";

export enum ReportsAction {
    GetReports = "GetReports"
}

type ReportsActionHandler<T> = (store: ActionContext<ReportsState, RunnerRootState>, payload: T) => void

export const actions: ActionTree<ReportsState, RunnerRootState> & Record<ReportsAction, ReportsActionHandler<any>> = {

    async [ReportsAction.GetReports](context, showAllReports: boolean) {
        const {rootState} = context

        const showAllParam = showAllReports
            ? "&show_all=true"
            : "";

        const query = rootState.git.metadata.git_supported
            ? `?branch=${rootState.git.selectedBranch}&commit=${rootState.git.selectedCommit}${showAllParam}`
            : "";

        await api<ReportsMutation, ReportsMutation>(context)
            .withSuccess(ReportsMutation.SetReports)
            .withError(ReportsMutation.SetReportsError)
            .get(`/reports/runnable/${query}`)
    }
}