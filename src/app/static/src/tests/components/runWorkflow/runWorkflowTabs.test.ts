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
        expect(sidebar.findAll("ul li a").at(0).text()).toBe("Run workflow")
        expect(sidebar.findAll("ul li a").at(0).element.classList).toContain("active")
        expect(sidebar.findAll("ul li a").at(1).text()).toBe("Workflow progress")
        expect(sidebar.findAll("ul li a").at(1).element.classList).not.toContain("active")
    })

    it(`it can render run tab-content`, async () => {
        const wrapper = getWrapper()
        const sidebar = wrapper.find("#sidebar")
        await sidebar.findAll("ul li a").at(0).trigger("click")
        expect(wrapper.find("#workflow-progress-tab").exists()).toBe(false)
        expect(wrapper.find("#run-workflow-tab").exists()).toBe(true)
        expect(wrapper.find("run-workflow-stub").exists()).toBe(true)
        expect(wrapper.find("#workflow-container").exists()).toBe(true)
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
        expect(wrapper.find("#workflow-progress-link").element.classList).toContain("active")
    })

    it(`can switch to run workflow tab and update workflowToRun on 'rerun' emit from progress component`, async () => {
        const wrapper = shallowMount(runWorkflowTabs, {
            data: () => {
               return {
                 selectedTab: "runWorkflowProgress"
               };
            }
        });
        expect(wrapper.find("#workflow-progress-link").element.classList).toContain("active");
        const workflowToRun = {name: "Test Workflow", reports: []};
        const progress = wrapper.find("run-workflow-progress-stub");
        await progress.vm.$emit("rerun", workflowToRun);
        const runWorkflow = wrapper.find("run-workflow-stub");
        expect(runWorkflow.props("workflowToRerun")).toBe(workflowToRun);
        expect(progress.exists()).toBe(false);
        expect(wrapper.find("#run-workflow-link").element.classList).toContain("active");
        expect(wrapper.find("#workflow-progress-link").element.classList).not.toContain("active");
    })
})
