import Vue from "vue";
import {shallowMount} from "@vue/test-utils";
import RunReportTabs from "../../../../js/components/vuex/runReport/runReportTabs.vue";
import Vuex from "vuex";
import {mockRunReportRootState} from "../../../mocks";
import {RunReportMutation} from "../../../../js/store/runReport/mutations";

describe("runReportTabs", () => {

    const switchTab = jest.fn();

    const createStore = () => {
        return new Vuex.Store({
            state: mockRunReportRootState(),
            mutations: {
                [RunReportMutation.SwitchTab]: switchTab
            }
        });
    };

    const getWrapper = () => {
        return shallowMount(RunReportTabs, {
            store: createStore()
        })
    }

    beforeEach(() => {
        jest.resetAllMocks();
    })

    it("renders tabs", () => {
        const wrapper = getWrapper()
        expect(wrapper.findAll(".nav-item").at(0).text()).toBe("Run a report");
        expect(wrapper.findAll(".nav-item").at(1).text()).toBe("Report logs");
        expect(wrapper.find("#run-tab").classes()).toEqual(["tab-pane", "active", "pt-4", "pt-md-1"]);
        expect(wrapper.find("#logs-tab").exists()).toBe(false);
    });

    it("renders correct tab", async () => {
        const wrapper = getWrapper()
        expect(wrapper.find("run-report-stub").exists()).toBe(true);
        expect(wrapper.find("report-log-stub").exists()).toBe(false);

        wrapper.vm.$store.state.selectedTab = "ReportLogs";

        await Vue.nextTick();

        expect(wrapper.find("run-report-stub").exists()).toBe(false);
        expect(wrapper.find("report-log-stub").exists()).toBe(true);
    });

    it("can switch tabs", async () => {
        const wrapper = getWrapper();
        await wrapper.findAll(".nav-item").at(1).find("a").trigger("click");
        expect(switchTab.mock.calls[0][1]).toBe("ReportLogs");

        await wrapper.findAll(".nav-item").at(0).find("a").trigger("click");
        expect(switchTab.mock.calls[1][1]).toBe("RunReport");
    });
});
