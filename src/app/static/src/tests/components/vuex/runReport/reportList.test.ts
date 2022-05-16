import {mount} from "@vue/test-utils";
import VueSelect from "vue-select";
import Vue from "vue";
import ReportList from "../../../../js/components/vuex/runReport/reportList.vue";
import Vuex from "vuex";
import {mockReportsState} from "../../../mocks";
import {ReportsState} from "../../../../js/store/reports/reports";

const report1 = {name: "report1", date: null};
const report2 = {name: "report2", date: new Date(2021, 3, 21, 9, 10).toISOString()};

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

function getWrapper() {
    return mount(ReportList,  {store: createStore({reports: [report1, report2]})});
}

describe("vuex reportList", () => {

    it("renders typeahead correctly and fires event on selection", async () => {
        const wrapper = getWrapper();

        (wrapper.findComponent(VueSelect).vm.$refs.search as any).focus();
        await Vue.nextTick();

        const reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(2);
        expect(reportSuggestions.at(0).text()).toBe("report1 Last run: never");
        expect(reportSuggestions.at(1).text()).toBe("report2 Last run: Wed Apr 21 2021, 09:10");

        (wrapper.findComponent(VueSelect).vm as any).select(report2);
    });

    it("typeahead filters list correctly", async () => {
        const wrapper = getWrapper();

        (wrapper.findComponent(VueSelect).vm.$refs.search as any).focus()

        await wrapper.find("input").setValue("rt2");
        let reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report2 Last run: Wed Apr 21 2021, 09:10");

        await wrapper.find("input").setValue("rt1");
        reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report1 Last run: never");
        expect(mockSelectReportMutation.mock.calls.length).toBe(2)
    });

});
