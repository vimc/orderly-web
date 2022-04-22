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
        console.log('selected branch', state.selectedBranch)
    },
    [GitMutation.SetCommits](state: GitState, payload: GitCommit[]) {
        state.commits = payload;
    },
    [GitMutation.SelectCommit](state: GitState, payload: string) {
        state.selectedCommit = payload;
        console.log('selected commit mutation', state.selectedCommit)
    }
}
