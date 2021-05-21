import {shallowMount} from "@vue/test-utils";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue"

describe(`runWorkflowReport`, () => {

    const getWrapper = () => {
        return shallowMount(runWorkflowReport, {propsData: {workflowMetadata: {}}})
    }

    it(`it renders workflow report headers correctly`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
        expect(wrapper.find("#git-header").text()).toBe("Git")
        expect(wrapper.find("#report-sub-header").text()).toBe("Reports")
    })

    it(`it renders workflow branch menu correctly`, () => {
        const wrapper = getWrapper()
        const branch = wrapper.find("#workflow-branch-div")
        expect(branch.find("label").text()).toBe("Branch")
        expect(branch.findAll("select option").length).toBe(1)
        expect(branch.find("select option").text()).toBe("master")
    })

    it(`it renders workflow commit menu correctly`, () => {
        const wrapper = getWrapper()
        const commit = wrapper.find("#workflow-commit-div")
        expect(commit.find("label").text()).toBe("Commit")
        expect(commit.findAll("select option").length).toBe(1)
        expect(commit.find("select option").text()).toBe("adfbd130 (2021-03-8 11:36:19)")
    })

    it(`it renders workflow preprocess menu correctly`, () => {
        const wrapper = getWrapper()
        const preprocessor = wrapper.find("#preprocess-div")
        expect(preprocessor.findAll("label").at(0).text()).toBe("Preprocess")
        expect(preprocessor.findAll("label").at(1).text()).toBe("nmin:")
        expect(preprocessor.findAll("label").at(2).text()).toBe("nmax:")

        expect(preprocessor.find("input#n-min").exists()).toBe(true)
        expect(wrapper.find("#workflow-remove-button").text()).toBe("Remove report")
        expect(preprocessor.find("input#n-max").exists()).toBe(true)
    })

    it(`it renders Add report menu correctly`, () => {
        const wrapper = getWrapper()
        const report = wrapper.find("#add-report-div")
        expect(report.find("label").text()).toBe("Add report")
        expect(report.find("input#workflow-report").exists()).toBe(true)
        expect(report.find("#add-report-button").text()).toBe("Add report")
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
    })
})