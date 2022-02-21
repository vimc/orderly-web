import {ReportsState} from "../../utils/types";
import {Module} from "vuex";
import {actions} from "./actions";
import {mutations} from "./mutations";

export const initialReportsState = (): ReportsState => {
    return {
        runnableReports: []
    }
};

const namespaced = true;

export const reports: Module<ReportsState, any> = {
    namespaced,
    state: initialReportsState(),
    actions,
    mutations
};
