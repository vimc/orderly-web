import Vue from "vue";
import Vuex, {StoreOptions} from "vuex";
import {git, GitState} from "../git/git";

export interface RunReportRootState {
    git: GitState
}

export const storeOptions: StoreOptions<RunReportRootState> = {
    state: {} as RunReportRootState,
    modules: {
        git
    }
};

Vue.use(Vuex);

export const store = new Vuex.Store<RunReportRootState>(storeOptions);
