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
                    entryStep: null,
                    backButtonVisible: true
                }
            }
        })
    }
    it(`display run workflow tab and can render workflow wizard on click event`, async () => {
        const wrapper = getWrapper()
        expect(wrapper.find(runWorkflowCreate).exists()).toBe(true)
        await wrapper.find("#create-workflow").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.vm.$data.entryStep).toBe("report")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.vm.$data.backButtonVisible).toBe(true)
    })

    it(`does not start workflow wizard when run workflow is rendered`, async () => {
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
        expect(wrapper.vm.$data.entryStep).toBe("report")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.vm.$data.backButtonVisible).toBe(true)
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from re-run`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})
        await wrapper.find("#rerun").trigger("click")

        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata")).toMatchObject(workflowMetadata)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata").placeholder).toBe("Placeholder works")

        expect(wrapper.vm.$data.entryStep).toBe("run")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.vm.$data.backButtonVisible).toBe(false)

        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Run workflow")

        //cancel workflow
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")

        await buttons.at(0).trigger("click")
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")

        expect(wrapper.find("#create-workflow-header").exists()).toBe(false)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.find("#create-workflow-header").exists()).toBe(true)
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from clone`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})
        await wrapper.find("#clone").trigger("click")

        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata")).toMatchObject(workflowMetadata)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata").placeholder).toBe("Placeholder works")

        expect(wrapper.vm.$data.entryStep).toBe("report")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.vm.$data.backButtonVisible).toBe(true)
        expect(wrapper.find("#add-report-header").text()).toBe("Add reports")

        const buttons = wrapper.findAll("button")

        expect(buttons.at(0).text()).toBe("Remove report")
        expect(buttons.at(1).text()).toBe("Add report")
        expect(buttons.at(2).text()).toBe("Cancel")
        expect(buttons.at(3).text()).toBe("Next")

        await buttons.at(3).trigger("click")
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        const runButtons = wrapper.findAll("button")

        expect(runButtons.at(0).text()).toBe("Cancel")
        expect(runButtons.at(1).text()).toBe("Back")
        expect(runButtons.at(2).text()).toBe("Run workflow")

        //cancel workflow
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")

        await runButtons.at(0).trigger("click")
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")

        expect(wrapper.find("#create-workflow-header").exists()).toBe(false)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.find("#create-workflow-header").exists()).toBe(true)
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from create`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})
        await wrapper.find("#create-workflow").trigger("click")

        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata")).toMatchObject(workflowMetadata)
        expect(wrapper.find(workflowWizard).props("runWorkflowMetadata").placeholder).toBe("Placeholder works")

        expect(wrapper.vm.$data.entryStep).toBe("report")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.vm.$data.backButtonVisible).toBe(true)
        expect(wrapper.find("#add-report-header").text()).toBe("Add reports")

        const buttons = wrapper.findAll("button")

        expect(buttons.at(0).text()).toBe("Remove report")
        expect(buttons.at(1).text()).toBe("Add report")
        expect(buttons.at(2).text()).toBe("Cancel")
        expect(buttons.at(3).text()).toBe("Next")

        await buttons.at(3).trigger("click")
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        const runButtons = wrapper.findAll("button")

        expect(runButtons.at(0).text()).toBe("Cancel")
        expect(runButtons.at(1).text()).toBe("Back")
        expect(runButtons.at(2).text()).toBe("Run workflow")

        //cancel workflow
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")

        await runButtons.at(0).trigger("click")
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")

        expect(wrapper.find("#create-workflow-header").exists()).toBe(false)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.find("#create-workflow-header").exists()).toBe(true)
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