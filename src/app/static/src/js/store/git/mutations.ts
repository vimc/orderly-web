import {MutationTree} from "vuex";
import {GitState} from "./git";
import {RunReportMetadata} from "../../utils/types";

export enum GitMutation {
    SetMetadata = "SetMetadata",
    SelectBranch = "SelectBranch",
    SelectCommitId = "SelectCommitId"
}

export const mutations: MutationTree<GitState> = {
    [GitMutation.SetMetadata](state: GitState, payload: RunReportMetadata) {
        state.metadata = payload.metadata;
        state.gitBranches = payload.git_branches;
        state.selectedBranch = state.gitBranches.length ? state.gitBranches[0] : ""
    },

    [GitMutation.SelectBranch](state: GitState, payload: string) {
        state.selectedBranch = payload
    },

    [GitMutation.SelectCommitId](state: GitState, payload: string) {
        state.selectedCommitId = payload
    }
}
