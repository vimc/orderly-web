import {RunnerRootState, RunReportMetadataDependency} from "../../utils/types";
import {Module} from "vuex";
import {mutations} from "./mutations";
import {actions} from "./actions";

export interface GitState {
    branches: string[],
    metadata: RunReportMetadataDependency
}

export const initialGitState = (): GitState => {
    return {
        metadata: null,
        branches: []
    }
};

const namespaced = true;

export const git: Module<GitState, RunnerRootState> = {
    namespaced,
    state: initialGitState(),
    actions,
    mutations
};
