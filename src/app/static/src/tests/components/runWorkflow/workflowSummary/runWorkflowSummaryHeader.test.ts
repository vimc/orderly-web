import {shallowMount} from "@vue/test-utils";
import runWorkflowSummaryHeader from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummaryHeader.vue"
import {AlertTriangleIcon} from "vue-feather-icons"

describe(`runWorkflowSummaryHeader`, () => {

    const summaryData = {
        missing_dependencies: {
            step2: ["step1"]
        },
        ref: "refNum",
        reports: [
            {
                name: "step2",
                params: {}
            }
        ]
    }

    const getWrapper = (workflowSummary = summaryData) => {
        return shallowMount(runWorkflowSummaryHeader, {propsData: {workflowSummary: workflowSummary}})
    }

    it(`it renders workflow summary header including reports that are missing dependencies`, () => {
        const wrapper = getWrapper();
        expect(wrapper.find("#summary-header").text()).toBe("Summary");
        const summaryWarning = wrapper.find("#summary-warning")
        expect(summaryWarning.find("div").find(AlertTriangleIcon).exists()).toBe(true);
        expect(summaryWarning.findAll("div").at(2).find("span").text()).toBe("Some reports depend on the latest version of other reports that are not included in your workflow:");
        const dependencyInstances = summaryWarning.findAll(".missingDependency")
        expect(dependencyInstances.length).toBe(1);
        expect(dependencyInstances.at(0).find("span").text()).toBe("step2");
        expect(dependencyInstances.at(0).findAll("li").length).toBe(1);
        expect(dependencyInstances.at(0).findAll("li").at(0).text()).toBe("step1");
    });

    it(`it renders workflow summary header but no missing dependencies if none`, () => {
        const wrapper = getWrapper(null);
        expect(wrapper.find("#summary-header").text()).toBe("Summary");
        const summaryWarning = wrapper.find("#summary-warning")
        expect(summaryWarning.exists()).toBe(false);
    });
})