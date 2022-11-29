import {mount, shallowMount} from "@vue/test-utils";
import runWorkflowCreate from "../../../js/components/runWorkflow/runWorkflowCreate.vue"
import {mockAxios} from "../../mockAxios";
import VueSelect from "vue-select";
import {mockRunWorkflowMetadata} from "../../mocks";
import Vue from "vue";

describe(`runWorkflowCreate`, () => {

    beforeEach(() => {
        mockAxios.reset()

        const url = "http://app/workflows"
        mockAxios.onGet(url)
            .reply(200, {"data": workflowSummaryMetadata});
    })

    const runnableWorkflowMetadata = [{
        name: "interim report",
        reports: [{"name": "reportA", "params": {"param1": "one", "param2": "two"}},
            {"name": "reportB", "params": {"param3": "three"}}],
        instances: {'name': 'value'},
        git_branch: "branch",
        git_commit: "commit",
        changelog: null,
    }]

    const clonedWorkflowMetadata = [{
        name: "",
        reports: [{"name": "reportA", "params": {"param1": "one", "param2": "two"}},
            {"name": "reportB", "params": {"param3": "three"}}],
        instances: {},
        git_branch: "branch",
        git_commit: "commit",
        changelog: null
    }]

    const workflowSummaryMetadata = [
        {name: "interim report", date: "2021-05-19T16:28:24Z", email: "test@example.com", key: "fake"},
        {name: "interim report2", date: "2021-06-19T16:28:24Z", email: "test@example.com2", key: "fake2"}
    ]

    const selectedWorkflow = [
        {name: "interim report", date: "2021-05-19T16:28:24Z", email: "test@example.com", key: "fake"}
    ]

    const workflowMetadata = {
        name: "interim report",
        date: "2021-05-19T16:28:24Z",
        email: "test@example.com",
        reports: [{"report": "reportA", "params": {"param1": "one", "param2": "two"}},
            {"report": "reportB", "params": {"param3": "three"}}],
        instances: {'name': 'value'},
        git_branch: "branch",
        git_commit: "commit",
        key: "fake"
    }
    const getWrapper = () => {
        return mount(runWorkflowCreate,
            {
                data() {
                    return {
                        error: null,
                        defaultMessage: null,
                        selectedWorkflow: null,
                        runWorkflowMetadata: null
                    }
                }
            })
    }

    it(`it renders page elements correctly`, () => {
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
        expect(wrapper.emitted("create")[0][0]).toStrictEqual(mockRunWorkflowMetadata());
    })

    it(`can emit re-run navigation step`, async () => {
        const wrapper = getWrapper()

        await Vue.nextTick();
        expect(wrapper.find("h2").text()).toBe("Run workflow")
        expect(mockAxios.history.get.length).toBe(1)
        expect(mockAxios.history.get[0].url).toBe("http://app/workflows")
        expect(wrapper.vm.$data.error).toStrictEqual("")
        expect(wrapper.vm.$data.defaultMessage).toStrictEqual("")
        expect(wrapper.vm.$data.workflows).toStrictEqual(workflowSummaryMetadata)

        const vueSelect = wrapper.findComponent(VueSelect)
        vueSelect.vm.$emit("input", selectedWorkflow)
        expect(vueSelect.find("input").attributes("placeholder")).toBe("Search by name or user...")
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})
        await wrapper.find("#rerun").trigger("click")
        expect(wrapper.emitted("rerun").length).toBe(1)
        expect(wrapper.emitted().rerun[0]).toEqual(runnableWorkflowMetadata)
    })

    it(`can emit clone navigation step`, async () => {
        const wrapper = getWrapper()
        await Vue.nextTick();
        expect(wrapper.find("h2").text()).toBe("Run workflow")
        expect(mockAxios.history.get.length).toBe(1)
        expect(mockAxios.history.get[0].url).toBe("http://app/workflows")
        expect(wrapper.vm.$data.error).toStrictEqual("")
        expect(wrapper.vm.$data.defaultMessage).toStrictEqual("")
        expect(wrapper.vm.$data.workflows).toStrictEqual(workflowSummaryMetadata)

        const vueSelect = wrapper.findComponent(VueSelect)
        vueSelect.vm.$emit("input", selectedWorkflow)
        expect(vueSelect.find("input").attributes("placeholder")).toBe("Search by name or user...")
        await wrapper.setData({runWorkflowMetadata: workflowMetadata})

        await wrapper.find("#clone").trigger("click")
        expect(wrapper.emitted("clone").length).toBe(1)
        expect(wrapper.emitted().clone[0]).toEqual(clonedWorkflowMetadata)
    })

    it(`does not enable buttons if workflow is not selected and metadata not populated`, async () => {
        const wrapper = getWrapper()

        await Vue.nextTick();

        expect(wrapper.find("h2").text()).toBe("Run workflow")
        expect(mockAxios.history.get.length).toBe(1)
        expect(mockAxios.history.get[0].url).toBe("http://app/workflows")
        expect(wrapper.vm.$data.error).toStrictEqual("")
        expect(wrapper.vm.$data.defaultMessage).toStrictEqual("")
        expect(wrapper.vm.$data.workflows).toStrictEqual(workflowSummaryMetadata)

        await wrapper.setData({runWorkflowMetadata: workflowMetadata, selectedWorkflow: null})
        expect(wrapper.find("#rerun").attributes("disabled")).toStrictEqual("disabled")
        expect(wrapper.find("#clone").attributes("disabled")).toStrictEqual("disabled")

        await wrapper.setData({runWorkflowMetadata: null, selectedWorkflow: selectedWorkflow})
        expect(wrapper.find("#rerun").attributes("disabled")).toStrictEqual("disabled")
        expect(wrapper.find("#clone").attributes("disabled")).toStrictEqual("disabled")
    })

    it(`does display error message if error when getting workflows`, (done) => {
        const url = "http://app/workflows"
        mockAxios.onGet(url)
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(runWorkflowCreate,
            {propsData: {runWorkflowMetadata: workflowMetadata}})

        setTimeout(async () => {
            expect(wrapper.find("h2").text()).toBe("Run workflow")
            expect(mockAxios.history.get.length).toBe(1)
            expect(mockAxios.history.get[0].url).toBe("http://app/workflows")
            expect(wrapper.vm.$data.error.response.data).toStrictEqual("TEST ERROR")
            expect(wrapper.vm.$data.defaultMessage).toStrictEqual("An error occurred while retrieving previously run workflows")
            expect(wrapper.vm.$data.workflows).toStrictEqual([])
            done()
        })
    })
})
