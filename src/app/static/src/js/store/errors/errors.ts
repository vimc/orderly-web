import {Module} from "vuex";
import {mutations} from "./mutations";
import {RunnerRootState, Error} from "../../utils/types";

export interface ErrorsState {
    errors: Error[]
}

export const initialErrorsState = (): ErrorsState => {
    return {
        errors: []
    }
};

const namespaced = true;

export const errors: Module<ErrorsState, RunnerRootState> = {
    namespaced,
    state: initialErrorsState(),
    mutations
};