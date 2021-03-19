import Vue from "vue";
import {shallowMount} from "@vue/test-utils";
import RunReportTabs from "../../../js/components/runReport/runReportTabs.vue";
import runningReportDetails from "../../../js/components/runReport/runningReportDetails.vue";

describe("runReportTabs", () => {

    const initialGitBranches = ["master", "dev"];

    const props = {
        metadata: {
            git_supported: true,
            instances_supported: false
        },
        initialGitBranches
    };

    const getWrapper = (selectedLogReportKey = "", propsData = props) => {
        return shallowMount(RunReportTabs, {
            propsData,
            data() {
                return {
                    selectedLogReportKey: selectedLogReportKey
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

        const runReportComponent = wrapper.find("run-report-stub")
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
    });

    it(`renders running report tab component correctly`,  async() => {
        const key = "fakeKey"
        const wrapper = getWrapper(key)

        const logsTab = wrapper.findAll(".nav-item").at(1).find("a");
        logsTab.trigger("click");
        await Vue.nextTick();

        expect(wrapper.findComponent(runningReportDetails).exists()).toBe(true)
        expect(wrapper.findComponent(runningReportDetails).props("reportKey")).toBe("fakeKey")
    })
    
});
