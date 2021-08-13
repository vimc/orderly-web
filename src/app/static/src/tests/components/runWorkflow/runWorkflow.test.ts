import {mount, shallowMount} from "@vue/test-utils";
import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import runWorkflow from '../../../js/components/runWorkflow/runWorkflow.vue'
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue";
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue";
import {emptyWorkflowMetadata} from "./runWorkflowCreate.test";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue";
import {runReportMetadataResponse} from "./runWorkflowReport/runWorkflowReport.test";
import {WorkflowRunReport} from "../../../js/utils/types";

describe(`runWorkflow`, () => {

    const selectedWorkflow = {name: "interim report", date: "2021-05-19T16:28:24Z", email: "test@example.com", key: "fake"}


    const workflowMetadata = {
        name: "interim report",
        key: "fake",
        email: "test@example.com",
        date: "2021-05-19T16:28:24Z",
        reports: [{"report": "reportA", "params": {"param1": "one", "param2": "two"}},
            {"report": "reportB", "params": {"param3": "three"}}],
        instances: {'name': 'value'},
        git_branch: "branch",
        git_commit: "commit"
    }

    beforeEach(() => {
        mockAxios.reset();

        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": runReportMetadataResponse});
    });

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

        expect(wrapper.find("#rerun").attributes("disabled")).toBeUndefined()
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
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
        expect(wrapper.find(workflowWizard).props("disableRename")).toBe(true)

        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Run workflow")
        expect(wrapper.find("#workflow-name-div input").attributes("readonly")).toBe("readonly")
        expect(wrapper.find("#next-workflow").attributes("disabled")).toBeUndefined()

        //cancel workflow
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")

        await buttons.at(0).trigger("click")
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")

        expect(wrapper.find("#create-workflow-header").exists()).toBe(false)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.find("#create-workflow-header").exists()).toBe(true)
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from clone`, async (done) => {
        const wrapper = getWrapper()
        //Enables rerun and clone buttons
        await wrapper.find(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })

        expect(wrapper.find(runWorkflowCreate).vm.$data.runWorkflowMetadata).toMatchObject(workflowMetadata)
        wrapper.find("#clone").trigger("click")

        setTimeout(async () => {
            expect(wrapper.vm.$data.workflowStarted).toBe(true)
            expect(wrapper.find(workflowWizard).exists()).toBe(true)

            expect(wrapper.find("#add-report-header").text()).toBe("Add reports")

            const buttons = wrapper.findAll("button")
            expect(buttons.at(0).text()).toBe("Refresh git")
            expect(wrapper.find("#cancel-workflow").text()).toBe("Cancel")
            expect(wrapper.find("#next-workflow").text()).toBe("Next")

        //cancel workflow
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")
            await wrapper.find(runWorkflowReport).vm.$emit("valid", true);
            await wrapper.find("#next-workflow").trigger("click")
            expect(wrapper.find("#run-header").text()).toBe("Run workflow")
            const runButtons = wrapper.findAll("button")

            expect(wrapper.find(workflowWizard).props("disableRename")).toBe(false)
            expect(wrapper.find("#workflow-name-div input").attributes("disabled")).toBeUndefined()

            expect(runButtons.at(0).text()).toBe("Cancel")
            expect(runButtons.at(1).text()).toBe("Back")
            expect(runButtons.at(2).text()).toBe("Run workflow")

            expect(wrapper.find("#next-workflow").attributes("disabled")).toBe("disabled")

            /**
             * this test ascertain that when a workflow is entered from a rerun step,
             * the name input should be disabled. While entry from any other step should be enabled.
             */
            await wrapper.find("#workflow-name-div input").setValue("interim workflow")
            expect(wrapper.find("#next-workflow").attributes("disabled")).toBeUndefined()

            //cancel workflow
            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")

            await runButtons.at(0).trigger("click")
            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")

            expect(wrapper.find("#create-workflow-header").exists()).toBe(false)
            await wrapper.find("#confirm-cancel-btn").trigger("click")
            expect(wrapper.find("#create-workflow-header").exists()).toBe(true)
            done()
        });
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from create`,  (done) => {
        const wrapper = getWrapper()
        wrapper.find("#create-workflow").trigger("click")

        setTimeout(async () => {
            expect(wrapper.vm.$data.runWorkflowMetadata).toStrictEqual(emptyWorkflowMetadata);
            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")
            expect(wrapper.find(workflowWizard).exists()).toBe(true)
            expect(wrapper.find(workflowWizard).props("initialRunWorkflowMetadata")).toMatchObject(emptyWorkflowMetadata);
            expect(wrapper.vm.$data.workflowStarted).toBe(true);

            const buttons = wrapper.find(workflowWizard).findAll("button")

            expect(buttons.at(0).text()).toBe("Refresh git")
            expect(buttons.at(1).text()).toBe("Cancel")
            expect(buttons.at(2).text()).toBe("Next")

            await wrapper.find(runWorkflowReport).vm.$emit("valid", true);
            await buttons.at(2).trigger("click")
            expect(wrapper.find("#run-header").text()).toBe("Run workflow")
            const runButtons = wrapper.findAll("button")

            /**
             * this test ascertain that when a workflow is entered from a rerun step,
             * the name input should be disabled. While entry from any other step should be enabled.
             */
            expect(wrapper.find(workflowWizard).props("disableRename")).toBe(false)
            expect(wrapper.find("#workflow-name-div input").attributes("readonly")).toBeUndefined()

            expect(runButtons.at(0).text()).toBe("Cancel")
            expect(runButtons.at(1).text()).toBe("Back")
            expect(runButtons.at(2).text()).toBe("Run workflow")

            expect(wrapper.find("#next-workflow").attributes("disabled")).toBe("disabled")
            await wrapper.find("#workflow-name-div input").setValue("interim workflow")
            expect(wrapper.find("#next-workflow").attributes("disabled")).toBeUndefined()

            //cancel workflow
            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")
            await runButtons.at(0).trigger("click")
            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")

            expect(wrapper.find("#create-workflow-header").exists()).toBe(false)
            await wrapper.find("#confirm-cancel-btn").trigger("click")
            expect(wrapper.find("#create-workflow-header").exists()).toBe(true)

            done();
        });
    })

    it(`handles rerun if workflowToRun is set`, async () => {
        const workflowToRerun = {name: "TEST WORKFLOW"};
        const wrapper = shallowMount(runWorkflow, {propsData: {workflowToRerun}});
        await Vue.nextTick();

        const wizard = wrapper.findComponent(workflowWizard);
        expect(wizard.props("initialRunWorkflowMetadata")).toBe(workflowToRerun);
        expect(wizard.props("steps")).toStrictEqual([{name: "run", component: "runWorkflowRun"}]);
        expect(wizard.props("disableRename")).toBe(true);
    });

    it(`does not handle rerun if worklflowToRun is not set`, async () => {
        const wrapper = shallowMount(runWorkflow);
        await Vue.nextTick();

        expect(wrapper.findComponent(workflowWizard).exists()).toBe(false);
        expect(wrapper.vm.$data.runWorkflowMetadata).toBe(null);
    });
})
