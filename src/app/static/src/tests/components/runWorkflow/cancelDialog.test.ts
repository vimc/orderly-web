import {mount} from "@vue/test-utils";
import cancelDialog from "../../../js/components/runWorkflow/cancelDialog.vue"

describe(`cancelDialog`, () => {

    const getWrapper = (showModal = false) => {
        return mount(cancelDialog,
            {
                propsData: {
                    showModal: showModal
                }
            })
    }

    it(`can render cancel dialog `, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#confirm-cancel-container").classes()).not.toContain("modal-show")
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-hide")

        const buttons = wrapper.findAll("button")
        expect(buttons.at(0).text()).toBe("Yes")
        expect(buttons.at(1).text()).toBe("No")

        expect(wrapper.find("#cancel-heading").text()).toBe("Confirm cancellation")
        expect(wrapper.find("#cancel-text").text()).toBe("Are you sure you want to cancel?")
    })

    it(`can open cancel dialog`, () => {
        const wrapper = getWrapper(true)
        expect(wrapper.find("#confirm-cancel-container").exists()).toBe(true)
        expect(wrapper.find("#confirm-cancel-container").classes()).not.toContain("modal-hide")
        expect(wrapper.find("#confirm-cancel-container").classes()).toContain("modal-show")
    })
})