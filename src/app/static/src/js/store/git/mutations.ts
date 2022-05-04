import { MutationTree } from "vuex";
import { GitState } from "./git";
import { GitCommit, RunReportMetadata } from "../../utils/types";

export enum GitMutation {
    SetMetadata = "SetMetadata",
    SelectBranch = "SelectBranch",
    SetCommits = "SetCommits",
    SelectCommit = "SelectCommit"
}

export const mutations: MutationTree<GitState> = {
    [GitMutation.SetMetadata](state: GitState, payload: RunReportMetadata) {
        state.metadata = payload.metadata;
        state.branches = payload.git_branches;
    },
    [GitMutation.SelectBranch](state: GitState, payload: string) {
        state.selectedBranch = payload;
    },
    [GitMutation.SetCommits](state: GitState, payload: GitCommit[]) {
        state.commits = payload;
        if (state.commits.length && !state.commits.some(commit => commit.id === state.selectedCommit)) {
            state.selectedCommit = state.commits[0].id
        }
        if (!state.commits.length) {
            state.selectedCommit = ""
        }
    },
    [GitMutation.SelectCommit](state: GitState, payload: string) {
        state.selectedCommit = payload;
    }
}
