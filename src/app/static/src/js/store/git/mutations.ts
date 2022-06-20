import { MutationTree } from "vuex";
import { GitState } from "./git";
import {GitCommit, RunReportMetadata} from "../../utils/types";

export enum GitMutation {
    SetMetadata = "SetMetadata",
    SelectBranch = "SelectBranch",
    SetFetchedGit = "SetFetchedGit",
    SetCommits = "SetCommits",
    SelectCommit = "SelectCommit",
    SetGitRefreshing = "SetGitRefreshing"
}

export const mutations: MutationTree<GitState> = {
    [GitMutation.SetMetadata](state: GitState, payload: RunReportMetadata) {
        state.metadata = payload.metadata;
        state.branches = payload.git_branches;
    },
    [GitMutation.SetFetchedGit](state: GitState, payload: string[]) {
        state.branches = payload;
        state.selectedBranch = "";
        state.commits = [];
        state.selectedCommit = "";
        state.gitRefreshing = false;
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
    },
    [GitMutation.SetGitRefreshing](state: GitState) {
        state.gitRefreshing = true;
    }
}
