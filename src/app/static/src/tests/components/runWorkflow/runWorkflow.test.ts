import {mount} from "@vue/test-utils";
import Vue from "vue";
import runWorkflow from '../../../js/components/runWorkflow/runWorkflow.vue'
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue";
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue";
import {emptyWorkflowMetadata} from "./runWorkflowCreate.test";

describe(`runWorkflow`, () => {

    const selectedWorkflow = [
        {name: "interim report", date: "2021-05-19T16:28:24Z", email: "test@example.com", key: "fake"}
    ]

    const workflowMetadata = [{
        name: "interim report",
        date: "2021-05-19T16:28:24Z",
        email: "test@example.com",
        reports: [{"name": "reportA", "params": {"param1": "one", "param2": "two"}},
            {"name": "reportB", "params": {"param3": "three"}}],
        instances: {'name': 'value'},
        git_branch: "branch",
        git_commit: "commit",
        key: "fake"
    }]

    const getWrapper = () => {
        return mount(runWorkflow, {
            data() {
                return {
                    runWorkflowMetadata:  null,
                    workflowStarted: false,
                    toggleFinalStepNextTo: "Run workflow",
                    stepComponents: null
                }
            }
        })
    }

    it(`does not start workflow wizard when run workflow is rendered`, async () => {
        const wrapper = getWrapper()
        expect(wrapper.find(workflowWizard).exists()).toBe(false)
    })

    it(`can cancel workflow wizard`, async () => {
        const wrapper = getWrapper()

        //Enables rerun and clone buttons
        await wrapper.find(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })

        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.vm.$data.workflowStarted).toBe(false)
        expect(wrapper.find(workflowWizard).exists()).toBe(false)
    })

    it(`can emit complete when on final step and run report is triggered`, async () => {
        const wrapper = getWrapper()

        //Enables rerun button
        await wrapper.find(runWorkflowCreate).setData(
            {
                runWorkflowMetadata: workflowMetadata,
                selectedWorkflow: selectedWorkflow
            })

        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)

        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Run workflow")

        //Enable run workflow button and trigger event
        await wrapper.find(workflowWizard).setData({validStep: true})
        await buttons.at(1).trigger("click")
        expect(wrapper.find(workflowWizard).emitted().complete.length).toBe(1)
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from re-run`, async () => {
        const wrapper = getWrapper()

        //Enables rerun button
        await wrapper.find(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })
        expect(wrapper.find(runWorkflowCreate).vm.$data.runWorkflowMetadata).toMatchObject(workflowMetadata)
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.vm.$data.workflowStarted).toBe(true)

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
        //Enables rerun and clone buttons
        await wrapper.find(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })

        expect(wrapper.find(runWorkflowCreate).vm.$data.runWorkflowMetadata).toMatchObject(workflowMetadata)
        await wrapper.find("#clone").trigger("click")
        expect(wrapper.find(workflowWizard).exists()).toBe(true)

        expect(wrapper.vm.$data.workflowStarted).toBe(true)
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
        await wrapper.find("#create-workflow").trigger("click")

        await Vue.nextTick();
        expect(wrapper.vm.$data.runWorkflowMetadata).toStrictEqual(emptyWorkflowMetadata);

        expect(wrapper.find(workflowWizard).exists()).toBe(true)
        expect(wrapper.find(workflowWizard).props("initialRunWorkflowMetadata")).toMatchObject(emptyWorkflowMetadata);

        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        const buttons = wrapper.find(workflowWizard).findAll("button")
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
})
