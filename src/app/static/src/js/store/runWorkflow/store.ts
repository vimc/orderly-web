import Vue from "vue";
import Vuex, {StoreOptions} from "vuex";
import {git, GitState} from "../git/git";
import {RunnerRootState} from "../../utils/types";

export interface RunWorkflowRootState extends RunnerRootState {}

export const storeOptions: StoreOptions<RunWorkflowRootState> = {
    state: {} as RunWorkflowRootState,
    modules: {
        git
    }
};

Vue.use(Vuex);

export const store = new Vuex.Store<RunWorkflowRootState>(storeOptions);
