import {mount} from "@vue/test-utils";
import runWorkflow from '../../../js/components/runWorkflow/runWorkflow.vue'
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue";

describe(`runWorkflow`, () => {

    const workflowMetadata = {
        placeholder: "Placeholder works"
    }
    const getWrapper = () => {
        return mount(runWorkflow)
    }
    it(`it can render workflow wizard`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata")).toMatchObject(workflowMetadata)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata").placeholder).toBe("Placeholder works")
    })
})