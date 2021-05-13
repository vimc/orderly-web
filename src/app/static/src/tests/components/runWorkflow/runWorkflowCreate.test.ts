import {shallowMount} from "@vue/test-utils";
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue"

describe(`runWorkflowCreate`, () => {

    const getWrapper = () => {
        return shallowMount(runWorkflowCreate)
    }

    it(`it renders page elements correctly`, () =>{
        const wrapper = getWrapper()
        expect(wrapper.find("h2").text()).toBe("Run workflow")
        const divs = wrapper.findAll("div")

        expect(divs.at(0).find("p").text()).toBe("Either:")
        expect(wrapper.find("#create-workflow").text()).toBe("Create a blank workflow")

        expect(wrapper.find("#report-list").find("p").text()).toBe("Or re-use an existing workflow:")

        expect(wrapper.find("#rerun").text()).toBe("Re-run workflow")
        expect(wrapper.find("#clone").text()).toBe("Clone workflow")
    })

    it(`can emit create navigation step`, async () => {
        const wrapper = getWrapper()
        expect(wrapper.find("h2").text()).toBe("Run workflow")
        await wrapper.find("#create-workflow").trigger("click")
        expect(wrapper.emitted("create").length).toBe(1)
    })

    it(`can emit run navigation step`, async() => {
        const wrapper = getWrapper()
        expect(wrapper.find("h2").text()).toBe("Run workflow")
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.emitted("rerun").length).toBe(1)
    })

    it(`can emit clone navigation step`, async() => {
        const wrapper = getWrapper()
        expect(wrapper.find("h2").text()).toBe("Run workflow")
        await wrapper.find("#clone").trigger("click")
        expect(wrapper.emitted("clone").length).toBe(1)
    })

})