import {mount} from "@vue/test-utils";
import runWorkflow from '../../../js/components/runWorkflow/runWorkflow.vue'
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue";
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue";

describe(`runWorkflow`, () => {

    const workflowMetadata = {
        placeholder: "Placeholder works"
    }
    const getWrapper = () => {
        return mount(runWorkflow, {
            data() {
                return {
                    runWorkflowMetadata:  null,
                    workflowStarted: false,
                    entryStep: null
                }
            }
        })
    }
    it(`display run workflow tab and can render workflow wizard onclick event`, async () => {
        const wrapper = getWrapper()
        expect(wrapper.find(runWorkflowCreate).exists()).toBe(true)
        await wrapper.find("#create-workflow").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.vm.$data.entryStep).toBe("report")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
    })

    it(`does not start workflow wizard on run workflow render`, async () => {
        const wrapper = getWrapper()
        expect(wrapper.find(workflowWizard).exists()).toBe(false)
    })

    it(`can render workflow wizard metadata`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})
        await wrapper.find("#create-workflow").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata")).toMatchObject(workflowMetadata)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata").placeholder).toBe("Placeholder works")
    })

    it(`can cancel workflow wizard`, async () => {
        const wrapper = getWrapper()
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.vm.$data.workflowStarted).toBe(false)
        expect(wrapper.find(workflowWizard).exists()).toBe(false)

    })
})