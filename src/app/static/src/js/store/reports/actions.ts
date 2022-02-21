import {ActionContext, ActionTree} from "vuex";
import {api} from "../../utils/api";
import {RunnerRootState, ReportsState} from "../../utils/types";
import {ReportsMutation} from "./mutations";

export enum ReportsAction {
    FetchRunnableReports = "FetchRunnableReports"
}

type ReportsActionHandler = (store: ActionContext<ReportsState, RunnerRootState>, payload?: any) => void

export const actions: ActionTree<ReportsState, RunnerRootState> & Record<ReportsAction, ReportsActionHandler> = {

    async [ReportsAction.FetchRunnableReports](context: ActionContext<ReportsState, RunnerRootState>,
                                               payload: {
                                                   branch: string,
                                                   commit: string,
                                                   showAll: boolean
                                               }) {
        const gitSupported = context.rootState.git.metadata.git_supported
        const showAllParam = payload.showAll ? "&show_all=true" : "";
        const query = gitSupported ? `?branch=${payload.branch}&commit=${payload.commit}${showAllParam}` : '';
        await api.get(`/reports/runnable/${query}`)
            .then(({data}) => {
                context.commit(ReportsMutation.SetRunnableReports, data.data)
            })
    }
}
