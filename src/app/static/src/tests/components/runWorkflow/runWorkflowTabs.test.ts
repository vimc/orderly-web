import {shallowMount} from "@vue/test-utils";
import runWorkflowTabs from '../../../js/components/runWorkflow/runWorkflowTabs.vue'
import Vue from 'vue'

describe(`runWorkflowTabs`, () => {

    const getWrapper = () => {
        return shallowMount(runWorkflowTabs)
    }

    it(`it can render workflow tabs`, () => {
        const wrapper = getWrapper()
        const sidebar = wrapper.find("#sidebar")
        expect(sidebar.find("#workflow-title").text()).toBe("Workflows")
        expect(sidebar.findAll("ul li a").at(0).text()).toBe("Run")
        expect(sidebar.findAll("ul li a").at(1).text()).toBe("Progress")
    })

    it(`it can render run tab-content`, async () => {
        const wrapper = getWrapper()
        const sidebar = wrapper.find("#sidebar")
        await sidebar.findAll("ul li a").at(0).trigger("click")
        expect(wrapper.find("#workflow-progress-tab").exists()).toBe(false)
        expect(wrapper.find("#run-workflow-tab").exists()).toBe(true)
        expect(wrapper.find("run-workflow-stub").exists()).toBe(true)
        expect(wrapper.find("#run-workflow-tab").find("h2").text()).toBe("Create workflow")
    })

    it(`it can render progress tab-content`, async () => {
        const wrapper = getWrapper()
        const sidebar = wrapper.find("#sidebar")
        sidebar.findAll("ul li a").at(1).trigger("click")
        await Vue.nextTick()
        expect(wrapper.find("#run-workflow-tab").exists()).toBe(false)
        expect(wrapper.find("#workflow-progress-tab").exists()).toBe(true)
        expect(wrapper.find("run-workflow-progress-stub").exists()).toBe(true)
        expect(wrapper.find("#workflow-progress-tab").find("h2").text()).toBe("Workflow progress")
    })
})