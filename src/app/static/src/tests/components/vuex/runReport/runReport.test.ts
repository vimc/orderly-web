import {shallowMount} from "@vue/test-utils";
import RunReport from "../../../../js/components/vuex/runReport/runReport.vue";
import GitSelections from "../../../../js/components/vuex/runReport/gitSelections.vue";
import {ReportsState} from "../../../../js/store/reports/reports";
import Vuex from "vuex";
import {mockReportsState} from "../../../mocks";
import ReportList from "../../../../js/components/vuex/runReport/reportList.vue";

describe("RunReport", () => {

    afterEach(() => {
        jest.clearAllMocks();
    });

    const mockSelectReportMutation = jest.fn();

    const createStore = (reportState: Partial<ReportsState> = {}) => {
        return new Vuex.Store({
            modules: {
                reports: {
                    namespaced: true,
                    state: mockReportsState(reportState),
                    mutations: mockSelectReportMutation()
                }
            }
        })
    }

    const getWrapper = () => {
        return shallowMount(RunReport, {store: createStore()})
    }

    it("renders header", async () => {
        const wrapper = getWrapper();
        expect(wrapper.find("h2").text()).toBe("Run a report");
    });

    it("renders GitSelections", async () => {
        const wrapper = getWrapper();
        expect(wrapper.findComponent(GitSelections).exists()).toBe(true);
    });

    it("renders Report", async () => {
        const wrapper = getWrapper();
        expect(wrapper.findComponent(ReportList).exists()).toBe(true);
        expect(mockSelectReportMutation.mock.calls.length).toBe(1)
    });
});
