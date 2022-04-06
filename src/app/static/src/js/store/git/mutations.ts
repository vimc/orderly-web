import {MutationTree} from "vuex";
import {GitState} from "./git";
import {RunReportMetadata} from "../../utils/types";

export enum GitMutation {
    SetMetadata = "SetMetadata"
}

export const mutations: MutationTree<GitState> = {
    [GitMutation.SetMetadata](state: GitState, payload: RunReportMetadata) {
        state.metadata = payload.metadata;
        state.branches = payload.git_branches;
    }
}
