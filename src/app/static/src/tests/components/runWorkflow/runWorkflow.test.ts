import {mount, shallowMount} from "@vue/test-utils";
import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import runWorkflow from '../../../js/components/runWorkflow/runWorkflow.vue'
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue";
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue";
import {session} from "../../../js/utils/session";
import {mockRunWorkflowMetadata, mockRunReportMetadata, mockGitState} from "../../mocks";
import runWorkflowSummary from "../../../js/components/runWorkflow/workflowSummary/runWorkflowSummary.vue";
import {GitState} from "../../../js/store/git/git";
import Vuex from "vuex";

describe(`runWorkflow`, () => {

    const selectedWorkflow = {
        name: "interim report",
        date: "2021-05-19T16:28:24Z",
        email: "test@example.com",
        key: "fake"
    }

    const runWorkflowMetadata = mockRunWorkflowMetadata({git_branch: "master"})

    const workflowMetadata = mockRunWorkflowMetadata({
        name: "interim report",
        reports: [{"name": "reportA", "params": {"param1": "one", "param2": "two"}},
            {"name": "reportB", "params": {"param3": "three"}}],
        instances: {'name': 'value'},
        git_branch: "branch",
        git_commit: "commit"
    });

    const gitState: GitState = {
        git_branches: ["master", "dev"],
        metadata: {
            changelog_types: ["public", "internal"],
            git_supported: true,
            instances_supported: true,
            instances: {
                source: ["src"],
                annex: ["one"]
            }
        }
    }

    const createStore = (state: Partial<GitState> = gitState) => {
        return new Vuex.Store({
            state: {},
            modules: {
                git: {
                    namespaced: true,
                    state: mockGitState(state)
                }
            }
        });
    };

    beforeEach(() => {
        mockAxios.reset();

        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": mockRunReportMetadata()});
    });

    const getWrapper = () => {
        return mount(runWorkflow, {
            store: createStore(),
            data() {
                return {
                    runWorkflowMetadata: null,
                    workflowStarted: false,
                    toggleFinalStepNextTo: "Run workflow",
                    stepComponents: null
                }
            }
        })
    }

    const getShallowWrapper = (props: any = {}) => {
        return shallowMount(runWorkflow, {
            store: createStore(),
            propsData: {
                runWorkflowMetadata: null,
                workflowStarted: false,
                toggleFinalStepNextTo: "Run workflow",
                stepComponents: null,
                ...props
            }
        })
    }

    it(`does not start workflow wizard when run workflow is rendered`, async () => {
        const wrapper = getWrapper()
        expect(wrapper.findComponent(workflowWizard).exists()).toBe(false)
    })

    it(`can cancel workflow wizard`, async () => {
        const wrapper = getWrapper()

        //Enables rerun and clone buttons
        await wrapper.findComponent(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })

        expect(wrapper.find("#rerun").attributes("disabled")).toBeUndefined()
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.findComponent(workflowWizard).exists()).toBe(true)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.vm.$data.workflowStarted).toBe(false)
        expect(wrapper.findComponent(workflowWizard).exists()).toBe(false)
    })

    it(`can emit complete when on final step and run report is triggered`, async () => {
        const wrapper = getWrapper()

        //Enables rerun button
        await wrapper.findComponent(runWorkflowCreate).setData(
            {
                runWorkflowMetadata: workflowMetadata,
                selectedWorkflow: selectedWorkflow
            })

        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.findComponent(workflowWizard).exists()).toBe(true)

        let buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Next")
        await buttons.at(1).trigger("click")

        buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Back")
        expect(buttons.at(2).text()).toBe("Run workflow")

        //Enable run workflow button and trigger event
        await wrapper.findComponent(workflowWizard).setData({validStep: true})
        await buttons.at(2).trigger("click")
        expect(wrapper.findComponent(workflowWizard).emitted().complete.length).toBe(1)
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from re-run`, async () => {
        const wrapper = getWrapper()
        //Enables rerun button
        await wrapper.findComponent(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })

        expect(wrapper.findComponent(runWorkflowCreate).vm.$data.runWorkflowMetadata).toMatchObject(workflowMetadata)
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.findComponent(workflowWizard).exists()).toBe(true)
        expect(wrapper.vm.$data.workflowStarted).toBe(true)
        expect(wrapper.findComponent(workflowWizard).props("disableRename")).toBe(true)

        expect(wrapper.find("#summary-header").exists()).toBe(true)
        let buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Next")

        await buttons.at(1).trigger("click")

        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Back")
        expect(buttons.at(2).text()).toBe("Run workflow")
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
        const mockSetReportsSource = jest.fn();
        session.setSelectedWorkflowReportSource = mockSetReportsSource;

        const wrapper = getWrapper()
        //Enables rerun and clone buttons
        await wrapper.findComponent(runWorkflowCreate).setData(
            {
                selectedWorkflow: selectedWorkflow,
                runWorkflowMetadata: workflowMetadata
            })

        expect(wrapper.findComponent(runWorkflowCreate).vm.$data.runWorkflowMetadata)
            .toMatchObject(workflowMetadata)
        wrapper.find("#clone").trigger("click")

        setTimeout(async () => {
            expect(wrapper.vm.$data.workflowStarted).toBe(true)
            expect(wrapper.findComponent(workflowWizard).exists()).toBe(true)

            // expect session workflow report mode to have been reset
            expect(mockSetReportsSource.mock.calls.length).toBe(1);
            expect(mockSetReportsSource.mock.calls[0][0]).toBe(null);

            expect(wrapper.find("#add-report-header").text()).toBe("Add reports")

            let buttons = wrapper.findAll("button")
            expect(buttons.at(0).text()).toBe("Refresh git")
            expect(wrapper.find("#cancel-workflow").text()).toBe("Cancel")
            expect(wrapper.find("#next-workflow").text()).toBe("Next")

            //cancel workflow
            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")
            await wrapper.findComponent(runWorkflowReport).vm.$emit("valid", true);
            await wrapper.find("#next-workflow").trigger("click")
            expect(wrapper.find("#summary-header").exists()).toBe(true)

            buttons = wrapper.findAll("button")
            expect(buttons.at(0).text()).toBe("Cancel")
            expect(buttons.at(1).text()).toBe("Back")
            expect(buttons.at(2).text()).toBe("Next")

            await buttons.at(2).trigger("click")

            expect(wrapper.find("#run-header").text()).toBe("Run workflow")
            const runButtons = wrapper.findAll("button")

            expect(wrapper.findComponent(workflowWizard).props("disableRename")).toBe(false)
            expect(wrapper.find("#workflow-name-div input").attributes("disabled")).toBeUndefined()

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
            done()
        });
    })

    it(`can start and cancel workflow wizard correctly when starting a workflow wizard from create`, (done) => {
        const mockSetReportsSource = jest.fn();
        session.setSelectedWorkflowReportSource = mockSetReportsSource;

        const wrapper = getWrapper()
        wrapper.find("#create-workflow").trigger("click")

        setTimeout(async () => {
            expect(wrapper.vm.$data.runWorkflowMetadata).toStrictEqual(runWorkflowMetadata);

            expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")
            expect(wrapper.findComponent(workflowWizard).exists()).toBe(true)
            expect(wrapper.findComponent(workflowWizard).props("initialRunWorkflowMetadata")).toMatchObject(runWorkflowMetadata);
            expect(wrapper.vm.$data.workflowStarted).toBe(true);

            // expect session workflow report mode to have been reset
            expect(mockSetReportsSource.mock.calls.length).toBe(1);
            expect(mockSetReportsSource.mock.calls[0][0]).toBe(null);

            let buttons = wrapper.findComponent(workflowWizard).findAll("button")

            expect(buttons.at(0).text()).toBe("Refresh git")
            expect(buttons.at(1).text()).toBe("Cancel")
            expect(buttons.at(2).text()).toBe("Next")

            await wrapper.findComponent(runWorkflowReport).vm.$emit("valid", true);
            await buttons.at(2).trigger("click")
            expect(wrapper.find("#summary-header").exists()).toBe(true)

            buttons = wrapper.findComponent(workflowWizard).findAll("button")

            expect(buttons.at(0).text()).toBe("Cancel")
            expect(buttons.at(1).text()).toBe("Back")
            expect(buttons.at(2).text()).toBe("Next")

            await buttons.at(2).trigger("click")
            expect(wrapper.find("#run-header").text()).toBe("Run workflow")
            const runButtons = wrapper.findAll("button")

            // When a workflow is entered from a rerun step the name input should be disabled
            // but when entering from the create step as here it should be enabled
            expect(wrapper.findComponent(workflowWizard).props("disableRename")).toBe(false)
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

    it(`can call workflow endpoint when on final step and generate link that emits key to workflow`, async (done) => {

        const runWorkflowResponse = {
            data: {
                workflow_key: "workflowKey"
            }
        }
        mockAxios.onPost('http://app/workflow')
            .reply(200, runWorkflowResponse);

        const wrapper = getShallowWrapper()
        await wrapper.find("run-workflow-create-stub").vm.$emit("create", mockRunWorkflowMetadata())
        const workflowWizard = wrapper.find("workflow-wizard-stub")
        expect(workflowWizard.exists()).toBe(true)
        workflowWizard.vm.$emit("update-run-workflow-metadata", workflowMetadata)
        expect(wrapper.vm.$data.runWorkflowMetadata).toBe(workflowMetadata)
        expect(wrapper.find("#view-progress-link").exists()).toBe(false)
        await workflowWizard.vm.$emit("complete")
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe("http://app/workflow");
            expect(mockAxios.history.post[0].data).toBe(JSON.stringify(workflowMetadata));
            expect(wrapper.vm.$data.createdWorkflowKey).toBe("workflowKey")
            expect(wrapper.find("#view-progress-link").text()).toBe("View workflow progress")
            wrapper.find("#view-progress-link > a").trigger("click")
            setTimeout(() => {
                expect(wrapper.emitted("view-progress")).toStrictEqual([["workflowKey"]])
                done()
            });
        });
    })

    it(`workflow progress link clears when metadata updates`, async (done) => {

        const runWorkflowResponse = {
            data: {
                workflow_key: "workflowKey"
            }
        }
        mockAxios.onPost('http://app/workflow')
            .reply(200, runWorkflowResponse);

        const wrapper = getShallowWrapper()
        await wrapper.find("run-workflow-create-stub").vm.$emit("create", mockRunWorkflowMetadata())
        const workflowWizard = wrapper.find("workflow-wizard-stub")
        workflowWizard.vm.$emit("update-run-workflow-metadata", workflowMetadata)
        await workflowWizard.vm.$emit("complete")
        setTimeout(() => {
            expect(wrapper.find("#view-progress-link").text()).toBe("View workflow progress")
            workflowWizard.vm.$emit("update-run-workflow-metadata", {...workflowMetadata, name: "new"})
            setTimeout(() => {
                expect(wrapper.find("#view-progress-link").exists()).toBe(false)
                done()
            });
        });
    })

    it(`error response from workflow endpoint generates error message and new metadata clears error`, async (done) => {
        mockAxios.onPost('http://app/workflow')
            .reply(500, "TEST ERROR");

        const wrapper = getShallowWrapper()
        await wrapper.find("run-workflow-create-stub").vm.$emit("create", mockRunWorkflowMetadata())
        const workflowWizard = wrapper.find("workflow-wizard-stub")
        workflowWizard.vm.$emit("update-run-workflow-metadata", workflowMetadata)
        await workflowWizard.vm.$emit("complete")
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(wrapper.vm.$data.createdWorkflowKey).toBe("")
            expect(wrapper.find("#view-progress-link").exists()).toBe(false)
            const errorMessage = wrapper.find("error-info-stub")
            expect(errorMessage.props("defaultMessage")).toBe("An error occurred while running the workflow")
            expect(errorMessage.props("apiError")).toBeTruthy()
            workflowWizard.vm.$emit("update-run-workflow-metadata", {...workflowMetadata, name: "new"})
            setTimeout(() => {
                expect(errorMessage.props("apiError")).toBe("")
                done()
            });
        });
    })

    it(`handles rerun if workflowToRun is set`, async () => {
        const workflowToRerun = {name: "TEST WORKFLOW"};
        const wrapper = getShallowWrapper({workflowToRerun});
        await Vue.nextTick();

        const wizard = wrapper.findComponent(workflowWizard);
        expect(wizard.props("initialRunWorkflowMetadata")).toBe(workflowToRerun);
        expect(wizard.props("steps")).toStrictEqual([
            {name: "summary", component: "runWorkflowSummary"},
            {name: "run", component: "runWorkflowRun"}]);
        expect(wizard.props("disableRename")).toBe(true);
    });

    it(`does not handle rerun if workflowToRun is not set`, async () => {
        const wrapper = getShallowWrapper()
        await Vue.nextTick();

        expect(wrapper.findComponent(workflowWizard).exists()).toBe(false);
        expect(wrapper.vm.$data.runWorkflowMetadata).toBe(null);
    });
})
