import Vue from "vue";
import {shallowMount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";
import RunReportTabs from "../../../js/components/runReport/runReportTabs.vue";

describe("runReportTabs", () => {

    const initialGitBranches = ["master", "dev"];

    const props = {
        metadata: {
            git_supported: true,
            instances_supported: false
        },
        initialGitBranches,
        initialReportName: "minimal"
    };

    const getWrapper = (selectedLogReportKey = "", propsData = props) => {
        return shallowMount(RunReportTabs, {
            propsData,
            data() {
                return {
                    selectedRunningReportKey: selectedLogReportKey
                }
            }
        });
    }

    it("renders outline correctly", () => {
        const wrapper = getWrapper()
        expect(wrapper.findAll(".nav-item").at(0).text()).toBe("Run a report");
        expect(wrapper.findAll(".nav-item").at(1).text()).toBe("Report logs");
        expect(wrapper.find("#run-tab").classes()).toEqual(["tab-pane", "active", "pt-4", "pt-md-1"]);
        expect(wrapper.find("#logs-tab").exists()).toBe(false);
    });

    it("renders run report component correctly", () => {
        const wrapper = getWrapper()
        const runReportComponent = wrapper.find("run-report-stub");
        expect(runReportComponent.attributes("initialgitbranches")).toEqual("master,dev");
        expect(runReportComponent.props("metadata")).toEqual(props.metadata);
        expect(runReportComponent.props("initialReportName")).toEqual(props.initialReportName);
    });

    it("tab panes switches to logs on click and renders logs", async () => {
        const wrapper = getWrapper()
        const logsTab = wrapper.findAll(".nav-item").at(1).find("a");
        logsTab.trigger("click");
        await Vue.nextTick();
        expect(wrapper.find("#run-tab").exists()).toBe(false);
        const logsPane = wrapper.find("#logs-tab")
        expect(logsPane.classes()).toEqual(["tab-pane", "active", "pt-4", "pt-md-1"]);
    });

    it("reportLogs receives run key prop", async () => {
        const wrapper = getWrapper("key1")
        await wrapper.setData({selectedTab: "reportLogs"})
        const reportLog = wrapper.find("report-log-stub")
        expect(reportLog.props("selectedRunningReportKey")).toBe("key1");
        wrapper.setData({selectedRunningReportKey: "key2", selectedTab: "reportLogs"})
        await Vue.nextTick();
        expect(wrapper.find("report-log-stub").props("selectedRunningReportKey")).toBe("key2");
        expect(wrapper.find("report-log-stub").exists()).toBe(true);
    });

    it("selects report key when reportRun emits update key event", () => {
        const wrapper = getWrapper();
        const runReport = wrapper.find(RunReport);
        runReport.vm.$emit("update:key", "emittedKey");
        expect(wrapper.vm.$data.selectedRunningReportKey).toBe("emittedKey");
    });
});
