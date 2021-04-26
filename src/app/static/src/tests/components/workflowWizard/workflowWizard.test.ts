import {mount, shallowMount} from "@vue/test-utils";
import workflowWizard from "../../../js/components/workflowWizard/workflowWizard.vue"
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue";
import step from "../../../js/components/workflowWizard/step.vue";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue";
import runWorkflowRun from "../../../js/components/runWorkflow/runWorkflowRun.vue";

describe(`workflowWizard`, () => {

    const steps = [
        {name: "create", number: 1, hide: false, component: "runWorkflowCreate"},
        {name: "report", number: 2, hide: false, component: "runWorkflowReport"},
        {name: "summary", number: 3, hide: true, component: "runWorkflowSummary"},
        {name: "run", number: 4, hide: false, component: "runWorkflowRun"}
    ]


    const getWrapper = (activeStep = 1) => {
        return mount(workflowWizard, {
                propsData: {
                    runWorkflowMetadata: {placeholder: "testdata"},
                },
                data() {
                    return {
                        activeStep: activeStep,
                        steps: steps,
                        initiatedRerun: false
                    }
                }
            }
        )
    }

    it(`can render default component and step buttons`, async() => {

        const createButtonVisibility = {run: false, cancel: false, next: false, back: false}
        const wrapper = getWrapper()

        const mockStep = wrapper.findAll(step)

        //Create component
        expect(mockStep.at(0).props("active")).toBe(true)
        expect(mockStep.at(0).props("hasVisibility")).toMatchObject(createButtonVisibility)
        expect(mockStep.at(0).find(runWorkflowCreate).exists()).toBe(true)

        //Report page buttons
        const reportButtonVisibility = {run: false, cancel: true, next: true, back: false}
        expect(mockStep.at(1).props("hasVisibility")).toMatchObject(reportButtonVisibility)
        expect(mockStep.at(0).find(runWorkflowReport).exists()).toBe(false)

        //Run page buttons
        const runButtonVisibility = {run: true, cancel: true, next: false, back: true}
        expect(mockStep.at(2).props("hasVisibility")).toMatchObject(runButtonVisibility)
        expect(mockStep.at(0).find(runWorkflowRun).exists()).toBe(false)
    })

    it(`can render report component and elements`, async() => {
        //Report step
        const activeStep = 2

        const wrapper = getWrapper(activeStep)
        expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
        expect(wrapper.vm.$props.runWorkflowMetadata).toMatchObject({placeholder: "testdata"})

        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Remove report")
        expect(buttons.at(1).text()).toBe("Add report")
        expect(buttons.at(2).text()).toBe("Cancel")
        expect(buttons.at(3).text()).toBe("Next")
    })

    it(`can trigger cancel from report component to create component`, async() => {
        //Activate report step
        const activeStep = 2

        const wrapper = getWrapper(activeStep)
        const buttons = wrapper.findAll("button")
        expect(buttons.at(2).text()).toBe("Cancel")

        await buttons.at(2).trigger("click")
        expect(wrapper.find("#create-workflow-header").text()).toBe("Run workflow")
    })

    it(`can trigger next from report component to previous component `, async() => {
        //Activate report step
        const activeStep = 2

        const wrapper = getWrapper(activeStep)
        const buttons = wrapper.findAll("button")
        expect(buttons.at(3).text()).toBe("Next")

        await buttons.at(3).trigger("click")
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
    })

    it(`can render report component and elements`, async() => {
        //Run step
        const activeStep = 4

        const wrapper = getWrapper(activeStep)
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
        expect(wrapper.vm.$props.runWorkflowMetadata).toMatchObject({placeholder: "testdata"})

        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Back")
        expect(buttons.at(2).text()).toBe("Run workflow")
    })

    it(`can trigger cancel from run component to create component`, async() => {
        //Activate run step
        const activeStep = 4

        const wrapper = getWrapper(activeStep)
        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Cancel")

        await buttons.at(0).trigger("click")
        expect(wrapper.find("#create-workflow-header").text()).toBe("Run workflow")
    })

    it(`can trigger back from run component to previous component `, async() => {
        //Activate run step
        const activeStep = 4

        const wrapper = getWrapper(activeStep)
        const buttons = wrapper.findAll("button")
        expect(buttons.at(1).text()).toBe("Back")

        await buttons.at(1).trigger("click")
        expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
    })

    it(`can trigger run from run component to progress tab`, async() => {
        //Activate run step
        const activeStep = 4

        const wrapper = getWrapper(activeStep)
        const buttons = wrapper.findAll("button")
        expect(buttons.at(2).text()).toBe("Run workflow")

        await buttons.at(2).trigger("click")
        //not linking yet
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
    })
})