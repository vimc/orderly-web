import {shallowMount} from "@vue/test-utils";
import runWorkflowRun from "../../../js/components/runWorkflow/runWorkflowRun.vue"

describe(`runWorkflowRun`, () => {
    const getWrapper = () => {
        return shallowMount(runWorkflowRun, {propsData: {workflowMetadata: {}}})
    }

    it(`it renders workflow run page correctly`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
    })

    it(`can render workflow-name elements`, () => {
        const wrapper = getWrapper()
        const workflowName = wrapper.find("#workflow-name-div")
        expect(workflowName.text()).toBe("Name")
        expect(workflowName.find("input#run-workflow-name").exists()).toBe(true)
    })

    it(`can render workflow-source elements`, () => {
        const wrapper = getWrapper()
        const source = wrapper.find("#workflow-source-div")
        expect(source.text()).toBe("Database \"source\"")
        expect(source.find("input#run-workflow-database_source").exists()).toBe(true)
    })

    it(`can render workflow-changelog elements`, () => {
        const wrapper = getWrapper()
        const changelog = wrapper.find("#workflow-changelog-div")
        expect(changelog.find("label").text()).toBe("Changelog message")
        expect(changelog.findAll("select option").length).toBe(1)
        expect(changelog.find("select option").text()).toBe("Interim update")
    })

    it(`can render workflow-changelog-type elements`, () => {
        const wrapper = getWrapper()
        const changelogType = wrapper.find("#workflow-changelog-type-div")
        expect(changelogType.text()).toBe("Changelog type")
        expect(changelogType.find("input#run-workflow-changelog-type").exists()).toBe(true)
    })

    it(`can render workflow-tags elements`, () => {
        const wrapper = getWrapper()
        const tags = wrapper.find("#workflow-tags-div")
        expect(tags.text()).toBe("Report version tags")
        expect(tags.find("input#run-workflow-report-version-tags").exists()).toBe(true)
    })

    it(`can render workflow-completion elements`, () => {
        const wrapper = getWrapper()
        const completion = wrapper.find("#workflow-completion-div")
        expect(completion.find("label").text()).toBe("Only commit reports on workflow completion")
        expect(completion.find("#run-workflow-ticked").exists()).toBe(true)
        expect(completion.find("#run-workflow-ticked p").text()).toBe("ticked")
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
    })
})