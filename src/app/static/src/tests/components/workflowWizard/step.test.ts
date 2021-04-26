import {shallowMount} from "@vue/test-utils";
import step from "../../../js/components/workflowWizard/step.vue"

describe(`step`, () => {

    const hasVisibility = {
        run: true,
        cancel: true,
        next: true,
        back: true
    }

    const getWrapper = (stepsNavigationVisibility = hasVisibility) => {
        return shallowMount(step, {
            propsData: {
                hasVisibility: stepsNavigationVisibility,
                active: true
            }
        })
    }

    it(`can show step buttons as expected`, () => {
        const wrapper = getWrapper()
        const buttons = wrapper.findAll("button")
        expect(buttons.length).toBe(4)
        expect(buttons.at(0).text()).toBe("Cancel")
        expect(buttons.at(1).text()).toBe("Back")
        expect(buttons.at(2).text()).toBe("Next")
        expect(buttons.at(3).text()).toBe("Run workflow")
    })

    it(`can hide step buttons as expected`, () => {
        const hideVisibility = {
            run: false,
            cancel: false,
            next: false,
            back: false
        }
        const wrapper = getWrapper(hideVisibility)
        const buttons = wrapper.findAll("button")
        expect(buttons.length).toBe(0)
    })

    it(`can emit cancel when click event triggered as expected`, async () => {
        const wrapper = getWrapper()
        const buttons = wrapper.findAll("button")
        await buttons.at(0).trigger("click")

        expect(wrapper.emitted("jump").length).toBe(1)
        expect(wrapper.emitted("jump")[0]).toMatchObject(["cancel"])
    })

    it(`can emit back when click event triggered as expected`, async () => {
        const wrapper = getWrapper()
        const buttons = wrapper.findAll("button")
        await buttons.at(1).trigger("click")

        expect(wrapper.emitted("jump").length).toBe(1)
        expect(wrapper.emitted("jump")[0]).toMatchObject(["back"])
    })

    it(`can emit next when click event triggered as expected`, async () => {
        const wrapper = getWrapper()
        const buttons = wrapper.findAll("button")
        await buttons.at(2).trigger("click")

        expect(wrapper.emitted("jump").length).toBe(1)
        expect(wrapper.emitted("jump")[0]).toMatchObject(["next"])
    })

    it(`can emit run when click event triggered as expected`, async () => {
        const wrapper = getWrapper()
        const buttons = wrapper.findAll("button")
        await buttons.at(3).trigger("click")

        expect(wrapper.emitted("jump").length).toBe(1)
        expect(wrapper.emitted("jump")[0]).toMatchObject(["run"])
    })
})