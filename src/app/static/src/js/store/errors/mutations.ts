import {MutationTree} from "vuex";
import {PayloadWithType, Error} from "../../utils/types";
import {ErrorsState} from "./errors";

export enum ErrorsMutation {
    ErrorAdded = "ErrorAdded"
}

export const mutations: MutationTree<ErrorsState> = {
    [ErrorsMutation.ErrorAdded](state: ErrorsState, action: PayloadWithType<Error>) {
        state.errors.push(action.payload);
    }
}
