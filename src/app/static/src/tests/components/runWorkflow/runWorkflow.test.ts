import {shallowMount} from "@vue/test-utils";
import runWorkflow from '../../../js/components/runWorkflow/runWorkflow.vue'

describe(`runWorkflow`, () => {

    const getWrapper = () => {
        return shallowMount(runWorkflow)
    }

    it(`it can render workflow page`, ()=> {
        const wrapper = getWrapper()
        expect(wrapper.find("p").text()).toBe("Run workflow is coming soon")
    })

})