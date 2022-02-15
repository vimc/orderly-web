import {ActionContext, ActionTree} from "vuex";
import {GitState} from "./git";
import {api} from "../../utils/api";
import {GitMutation} from "./mutations";

export enum GitAction {
    FetchMetadata = "FetchMetadata"
}

type GitActionHandler = (store: ActionContext<GitState, any>) => void

export const actions: ActionTree<GitState, any> & Record<GitAction, GitActionHandler> = {

    async [GitAction.FetchMetadata](context) {
        await api.get('/report/run-metadata')
            .then(({data}) => {
                context.commit(GitMutation.SetMetadata, data.data)
            })
    }
}
