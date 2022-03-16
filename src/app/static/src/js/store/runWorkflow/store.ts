import Vue from "vue";
import Vuex, {StoreOptions} from "vuex";
import {git} from "../git/git";
import {RunnerRootState} from "../../utils/types";

export const storeOptions: StoreOptions<RunnerRootState> = {
    state: {} as RunnerRootState,
    modules: {
        git
    }
};

Vue.use(Vuex);

export const store = new Vuex.Store<RunnerRootState>(storeOptions);
