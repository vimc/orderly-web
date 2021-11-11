import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../js/components/runWorkflow/runWorkflowSummary.vue"
import {RunWorkflowMetadata} from "../../../js/utils/types";

describe(`runWorkflowSummary`, () => {

    const getWrapper = (meta: Partial<RunWorkflowMetadata> = {reports: []}) => {
        return shallowMount(runWorkflowSummary, {propsData: {workflowMetadata: meta}})
    }

    it(`it renders workflow summary page with multiple reports`, () => {
        const wrapper = getWrapper({
            reports: [{name: "r1"}, {name: "r2"}]
        });
        expect(wrapper.find("#summary-header").text()).toBe("Summary");
        expect(wrapper.find("span").text()).toBe("Your workflow contains 2 reports:");
        expect(wrapper.findAll("li").length).toBe(2);
        expect(wrapper.findAll("li").at(0).text()).toBe("r1");
        expect(wrapper.findAll("li").at(1).text()).toBe("r2");
    });

    it(`it renders workflow summary page with single report`, () => {
        const wrapper = getWrapper({
            reports: [{name: "r1"}]
        });
        expect(wrapper.find("#summary-header").text()).toBe("Summary");
        expect(wrapper.find("span").text()).toBe("Your workflow contains 1 report:");
        expect(wrapper.findAll("li").length).toBe(1);
        expect(wrapper.findAll("li").at(0).text()).toBe("r1");
    });

    it(`emits valid event on mount`, () => {
        const wrapper = getWrapper({
            reports: [{name: "r1"}]
        });
        expect(wrapper.emitted().valid[0][0]).toBe(true);
    });

})