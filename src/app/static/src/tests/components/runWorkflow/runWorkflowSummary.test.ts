import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../js/components/runWorkflow/runWorkflowSummary.vue"

describe(`runWorkflowSummary`, () => {
    const getWrapper = () => {
        return shallowMount(runWorkflowSummary, {propsData: {workflowMetadata: {}}})
    }

    it(`it renders workflow summary page correctly`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#summary-header").text()).toBe("Summary")
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
    })
})