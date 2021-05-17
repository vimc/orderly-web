import {shallowMount} from "@vue/test-utils";
import step from "../../../js/components/workflowWizard/step.vue"
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue"

describe(`step`, () => {

    const buttonVisibility = {
        next: true,
        back: true
    }

    const getWrapper = (stepsNavigationVisibility = buttonVisibility) => {
        return shallowMount(step, {
            propsData: {
                buttonVisibility: stepsNavigationVisibility,
                active: true,
                valid: false,
                submitLabel: null
            },
            slots: {default: runWorkflowReport}
        })
    }

    it(`can show report step buttons as expected`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#cancel-workflow").text()).toBe("Cancel")
        expect(wrapper.find("#previous-workflow").text()).toBe("Back")
        expect(wrapper.find("#next-workflow").text()).toBe("Next")
    })

    it(`can display default workflow setup for next button in final step`, async () => {
        const wrapper = getWrapper()
        await wrapper.setProps({buttonVisibility: {next: false}})
        expect(wrapper.find("#next-workflow").text()).toBe("Submit")
    })

    it(`can toggle workflow from next to Run workflow button in final step`, async () => {
        const wrapper = getWrapper()
        await wrapper.setProps({buttonVisibility: {next: false}, submitLabel: "Run workflow"})
        expect(wrapper.find("#next-workflow").text()).toBe("Run workflow")
    })

    it(`can disable workflow back/next button`, async () => {
        const wrapper = getWrapper()
        await wrapper.setProps({valid: false})
        expect(wrapper.find("#next-workflow").classes("disabled")).toBe(true)
    })

    it(`can emit cancel when click event gets triggered`, async () => {
        const wrapper = getWrapper()
        await wrapper.find("#cancel-workflow").trigger("click")
        expect(wrapper.emitted("cancel").length).toBe(1)
    })

    it(`can emit back when click event triggered as expected`, async () => {
        const wrapper = getWrapper()
        await wrapper.find("#previous-workflow").trigger("click")
        expect(wrapper.emitted("back").length).toBe(1)
    })

    it(`can emit next when click event triggered as expected`, async () => {
        const wrapper = getWrapper()
        await wrapper.find("#next-workflow").trigger("click")
        expect(wrapper.emitted("next").length).toBe(1)
    })
})