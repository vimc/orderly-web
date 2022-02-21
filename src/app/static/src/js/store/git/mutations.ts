import {MutationTree} from "vuex";
import {RunReportMetadata, GitState} from "../../utils/types";

export enum GitMutation {
    SetMetadata = "SetMetadata"
}

export const mutations: MutationTree<GitState> = {
    [GitMutation.SetMetadata](state: GitState, payload: RunReportMetadata) {
        state.metadata = payload.metadata;
        state.gitBranches = payload.git_branches;
    }
}
