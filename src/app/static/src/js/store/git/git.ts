import { GitCommit, RunnerRootState, RunReportMetadataDependency } from "../../utils/types";
import { Module } from "vuex";
import { mutations } from "./mutations";
import { actions } from "./actions";

export interface GitState {
    branches: string[],
    metadata: RunReportMetadataDependency
    selectedBranch: string
    commits: GitCommit[]
    selectedCommit: string
    gitRefreshing: boolean
}

export const initialGitState = (): GitState => {
    return {
        metadata: null,
        branches: [],
        selectedBranch: "",
        commits: [],
        selectedCommit: "",
        gitRefreshing: false
    }
};

const namespaced = true;

export const git: Module<GitState, RunnerRootState> = {
    namespaced,
    state: initialGitState(),
    actions,
    mutations
};
