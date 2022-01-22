import {shallowMount, Wrapper} from "@vue/test-utils";
import workflowReportLogDialog from "../../../js/components/runWorkflow/workflowReportLogDialog.vue";
import RunningReportDetails from "../../../js/components/reportLog/runningReportDetails.vue";

describe("workflowReportLogDialog", () => {
    const getWrapper = (workflowKey: string | null, reportKey: string | null) => {
        return shallowMount(workflowReportLogDialog, {
            propsData: {
                workflowKey,
                reportKey
            }
        });
    };

    it("renders as expected when workflow and report keys are set", () => {
        const wrapper = getWrapper("test-workflow", "test-report");
        const container = wrapper.find("#workflow-report-log-container");
        expect(container.classes()).toStrictEqual(["modal-background", "modal-show"]);

        expect(wrapper.find(".modal-header").text()).toBe("Report Log");

        const details = wrapper.findComponent(RunningReportDetails);
        expect(details.props("reportKey")).toBe("test-report");
        expect(details.props("workflowKey")).toBe("test-workflow");

        expect(wrapper.find("button#workflow-report-log-close").text()).toBe("OK");
    });

    const expectRendersHidden = (wrapper: Wrapper<any>) => {
        const container = wrapper.find("#workflow-report-log-container");
        expect(container.classes()).toStrictEqual(["modal-background", "modal-hide"]);
        expect(container.findComponent(RunningReportDetails).exists()).toBe(false);
    };

    it("renders as expected when workflow key is not set", () => {
        const wrapper = getWrapper(null, "test-report");
        expectRendersHidden(wrapper);
    });

    it("renders as expected when report key is not set", () => {
        const wrapper = getWrapper("test-workflow", null);
        expectRendersHidden(wrapper)
    });

    it("emits close event when OK button clicked", () => {
        const wrapper = getWrapper("test-workflow", "test-report");
        wrapper.find("button#workflow-report-log-close").trigger("click");
        expect(wrapper.emitted().close.length).toBe(1);
    });
});
