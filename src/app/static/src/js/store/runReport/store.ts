import {StoreOptions} from "vuex";
import {git} from "../git/git";

export interface RunReportRootState {
}

export const storeOptions: StoreOptions<RunReportRootState> = {
    state: {},
    modules: {
        git
    }
};
