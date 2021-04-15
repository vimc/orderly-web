import Vue from "vue";
import {shallowMount, mount} from "@vue/test-utils";
import RunReportTabs from "../../../js/components/runReport/runReportTabs.vue";
import ReportLog from "../../../js/components/reportLog/reportLog.vue";
import RunningReportsList from "../../../js/components/reportLog/runningReportsList.vue";

describe("runReportTabs", () => {

    const initialGitBranches = ["master", "dev"];

    const props = {
        metadata: {
            git_supported: true,
            instances_supported: false
        },
        initialGitBranches
    };

    const getWrapper = (propsData = props) => {
        return shallowMount(RunReportTabs, {
            propsData
        });
    }

    const data = {
        ...props,
        data() {
            return {
        selectedRunningReportKey: "key1", selectedTab: "reportLogs"
            }}
    }

    const getWrapper2 = (propsData = props) => {
        return shallowMount(RunReportTabs, data);
    }

    const getWrapper3 = (propsData = props) => {
        return mount(RunReportTabs, data);
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

    });

    it("tab panes switches to logs on click and renders logs", async () => {

        const wrapper = getWrapper()

        const logsTab = wrapper.findAll(".nav-item").at(1).find("a");
        logsTab.trigger("click");
        await Vue.nextTick();

        expect(wrapper.find("#run-tab").exists()).toBe(false);
        const logsPane = wrapper.find("#logs-tab")
        expect(logsPane.classes()).toEqual(["tab-pane", "active", "pt-4", "pt-md-1"]);
        expect(wrapper.find("report-log-stub").exists()).toBe(true);
    });

    it("reportLogs receives run key prop", async () => {

        const wrapper = getWrapper2()
        const reportLog = wrapper.find("report-log-stub")
        expect(reportLog.props("selectedRunningReportKey")).toBe("key1");
        wrapper.setData({selectedRunningReportKey: "key2", selectedTab: "reportLogs"})
        await Vue.nextTick();
        expect(wrapper.find("report-log-stub").props("selectedRunningReportKey")).toBe("key2");
        // reportLog.vm.$emit("selectedRunningReportKey", "key3")
        // // await Vue.nextTick();
        // await wrapper.vm.$nextTick()
        // expect(wrapper.vm.$data).toBe("key1");
    });

    it("reportLogs receives run key prop", async () => {

        const wrapper = getWrapper3()
        // await Vue.nextTick();
        const reportLog = wrapper.findComponent(ReportLog)
        const runningReportsList = wrapper.findComponent(RunningReportsList)
        expect(reportLog.html()).toBe("key1");
        // expect(wrapper.find("report-log-stub").props("selectedRunningReportKey")).toBe("key1");
        // expect(reportLog.attributes()).toBe("key1");
        // wrapper.setData({selectedRunningReportKey: "key2", selectedTab: "reportLogs"})
        // await Vue.nextTick();
        // expect(wrapper.find("report-log-stub").props("selectedRunningReportKey")).toBe("key2");
        reportLog.vm.$emit("key", "key2");
        await Vue.nextTick();
        // expect(reportLog.emitted()).toBe("key2");
        expect(wrapper.vm.$data).toBe("key2");
    });
    
});
