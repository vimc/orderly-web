import {ActionContext, ActionTree} from "vuex";
import {api} from "../../utils/api";
import {GitMutation} from "./mutations";
import {RunnerRootState, GitState} from "../../utils/types";

export enum GitAction {
    FetchMetadata = "FetchMetadata"
}

type GitActionHandler = (store: ActionContext<GitState, RunnerRootState>) => void

export const actions: ActionTree<GitState, RunnerRootState> & Record<GitAction, GitActionHandler> = {

    async [GitAction.FetchMetadata](context) {
        await api.get('/report/run-metadata')
            .then(({data}) => {
                context.commit(GitMutation.SetMetadata, data.data)
            })
    }
}
