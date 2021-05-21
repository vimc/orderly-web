import {mount, shallowMount} from "@vue/test-utils";
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue"
import step from "../../../js/components/workflowWizard/step.vue";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue";
import runWorkflowRun from "../../../js/components/runWorkflow/runWorkflowRun.vue";

describe(`workflowWizard`, () => {
    const steps = [
        {name: "report", component: "runWorkflowReport"},
        {name: "run", component: "runWorkflowRun"}
    ]

    const getWrapper = (mockStep = steps) => {
        return mount(workflowWizard, {
                propsData: {
                    runWorkflowMetadata: {placeholder: "testdata"},
                    steps: mockStep
                },
                data() {
                    return {
                        validStep: false,
                        showModal: false
                    }
                }
            }
        )
    }

    it(`can render first step, component and buttons correctly`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({activeStep: 0})
        const getSteps = wrapper.findAll(step)
        const mockButtonVisibility = {back: false}
        expect(getSteps.at(0).find(runWorkflowReport).exists()).toBe(true)
        expect(getSteps.at(0).props("buttonOptions")).toMatchObject(mockButtonVisibility)
    })

    it(`can render final step component and buttons correctly`, async () => {
        const wrapper = getWrapper()
        const finalStepIndex = steps.length-1
        await wrapper.setData({activeStep: finalStepIndex})

        const buttons = wrapper.findAll("button")

        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Back")
        expect(buttons.at(2).text()).toBe("Submit")

        const getSteps = wrapper.findAll(step)
        expect(getSteps.at(finalStepIndex).find(runWorkflowRun).exists()).toBe(true)
    })

    it(`can render final step, component and buttons correctly when re-running a workflow`, async () => {
        const mockStep = [
            {name: "run", component: "runWorkflowRun"}
        ]
        const wrapper = getWrapper(mockStep)
        const buttons = wrapper.findAll("button")

        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Submit")

        const getSteps = wrapper.findAll(step)
        expect(getSteps.at(mockStep.length-1).find(runWorkflowRun).exists()).toBe(true)
    })

    it(`can render default propsData on steps correctly`, async () => {

        const wrapper =  shallowMount(workflowWizard, {
            propsData: {
                runWorkflowMetadata: {placeholder: "testdata"},
                steps: steps
            },
            data() {
                return {
                    showModal: false
                }
            }
        })

        const getSteps = wrapper.findAll(step)
        expect(getSteps.length).toBe(2)

        //first step
        expect(getSteps.at(0).props().buttonOptions).toMatchObject({back: false})
        expect(getSteps.at(0).find("runworkflowreport-stub").props().workflowMetadata)
            .toMatchObject({"placeholder": "testdata"})

        //Final step
        expect(getSteps.at(1).props().buttonOptions).toMatchObject({back: true})
        expect(getSteps.at(1).find("runworkflowrun-stub").props().workflowMetadata)
            .toMatchObject({"placeholder": "testdata"})
    })

    it(`can render report component`, async() => {
        const wrapper = getWrapper()
        await wrapper.setData({activeStep: 0})
        expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
        expect(wrapper.vm.$props.runWorkflowMetadata).toMatchObject({placeholder: "testdata"})

        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Remove report")
        expect(buttons.at(1).text()).toBe("Add report")
        expect(buttons.at(2).text()).toBe("Cancel")
        expect(buttons.at(3).text()).toBe("Next")
    })

    it(`can render run component`, async() => {
        const wrapper = getWrapper()

        //run step is usually the final step
        const finalStepIndex = steps.length-1
        await wrapper.setData({activeStep: finalStepIndex})

        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        expect(wrapper.vm.$props.runWorkflowMetadata).toMatchObject({placeholder: "testdata"})

        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Back")
        expect(buttons.at(2).text()).toBe("Submit")
    })

    it(`can go to the next step`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({activeStep: 0})
        const buttons = wrapper.findAll("button")

        expect(buttons.at(3).text()).toBe("Next")

        await buttons.at(3).trigger("click")
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
    })

    it(`can go to previous step `, async (done) => {
        const wrapper = getWrapper()
        await wrapper.setData({activeStep: 1})

        const buttons = wrapper.findAll("button")
        expect(buttons.at(1).text()).toBe("Back")

        await buttons.at(1).trigger("click")
        setTimeout(() => {
            expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
            done()
        })
    })

    it(`can emit cancel event from report step as expected`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({activeStep: 0})
        const buttons = wrapper.findAll("button")
        expect(buttons.at(2).text()).toBe("Cancel")

        expect(wrapper.vm.$data.showModal).toBe(false)
        await buttons.at(2).trigger("click")

        expect(wrapper.vm.$data.showModal).toBe(true)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.emitted().cancel.length).toBe(1)
    })

    it(`can emit cancel event from run step as expected`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({activeStep: 1})
        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")

        expect(wrapper.vm.$data.showModal).toBe(false)
        await buttons.at(0).trigger("click")

        expect(wrapper.vm.$data.showModal).toBe(true)
        await wrapper.find("#confirm-cancel-btn").trigger("click")
        expect(wrapper.emitted().cancel.length).toBe(1)
    })

    it(`can add a new component to steps and display buttons as expected`, async () => {
        const newSteps = [
            {name: "report", component: "runWorkflowReport"},
            {name: "summary", component: "testComponent"},
            {name: "run", component: "runWorkflowRun"}
        ]

        const mockHasVisibility = {back: true}
        const wrapper =  shallowMount(workflowWizard, {
            propsData: {
                runWorkflowMetadata: {placeholder: "testdata"},
                steps: newSteps,
            },
            data() {
                return {
                    activeStep: 0,
                    showModal: false
                }
            }
        })

        const getSteps = wrapper.findAll(step)
        expect(getSteps.length).toBe(3)

        //newly added step
        expect(getSteps.at(1).props().buttonOptions).toMatchObject(mockHasVisibility)
        expect(getSteps.at(1).find("testComponent").exists()).toBe(true)
    })

    it(`can toggle final step button`, async () => {
        const mockStep = [
            {name: "run", component: "runWorkflowRun"}
        ]
        const wrapper = getWrapper(mockStep)
        await wrapper.setProps({submitLabel: "Any name"})
        const buttons = wrapper.findAll("button")

        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Any name")

        const getSteps = wrapper.findAll(step)
        expect(getSteps.at(mockStep.length-1).find(runWorkflowRun).exists()).toBe(true)
    })
})