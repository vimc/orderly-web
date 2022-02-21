import {GitState} from "../../utils/types";
import {Module} from "vuex";
import {mutations} from "./mutations";
import {actions} from "./actions";

export const initialGitState = (): GitState => {
    return {
        metadata: null,
        gitBranches: []
    }
};

const namespaced = true;

export const git: Module<GitState, any> = {
    namespaced,
    state: initialGitState(),
    actions,
    mutations
};
