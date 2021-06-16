import {shallowMount, mount} from "@vue/test-utils";
import runWorkflowProgress from '../../../js/components/runWorkflow/runWorkflowProgress.vue'
import {mockAxios} from "../../mockAxios";
import Vue from "vue";
// import vSelect from "vue-select";

const workflows = {
    "status": "success",
    "errors": null,
    "data": [
        {date: "time1",
        key: "key1",
        name: "name1",
        email: "email1"},
        {date: "time2",
        key: "key2",
        name: "name2",
        email: "email2"}
    ]
  }

const workflowStatus1 = {
    "status": "success",
    "errors": null,
    "data": {
      "status": "running",
      "reports": [
        {
          "key": "preterrestrial_andeancockoftherock",
          "name": "report one a",
          "status": "error",
          "date": "2021-06-16T09:51:16Z"
        },
        {
          "key": "hygienic_mammoth",
          "name": "report two a",
          "status": "success",
          "version": "20210510-100458-8f1a9624",
          "date": "2021-06-16T09:51:16Z"
        },
        {
          "key": "blue_bird",
          "name": "report three a",
          "status": "running",
          "date": null
        }
      ]
    }
  }


describe(`runWorkflowProgress`, () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/workflows')
            .reply(200, workflows);
        mockAxios.onGet('http://app/workflows/key1/status')
            .reply(200, workflowStatus1);
    });

    const getWrapper = () => {
        return shallowMount(runWorkflowProgress, {propsData: {workflowMetadata: {}}})
    }

    // const getFullWrapper = () => {
    //     return mount(runWorkflowProgress, {propsData: {workflowMetadata: {}}})
    // }

    it(`it can render if no workflows returned`, async () => {
        mockAxios.onGet('http://app/workflows')
            .reply(200, null);
        const wrapper = getWrapper()
        
        await Vue.nextTick()
        await Vue.nextTick()
        await Vue.nextTick()
        expect(wrapper.find("p").text()).toBe("No workflows to show")
    })

    it(`it can render runWorkflowProgress page`, async () => {
        const wrapper = getWrapper()
        
        await Vue.nextTick()
        await Vue.nextTick()
        await Vue.nextTick()
        expect(wrapper.find("label").text()).toBe("Workflow")
        expect(wrapper.find("v-select-stub").attributes("placeholder")).toBe("Search by name...")
        expect(wrapper.findAll("button").at(0).text()).toBe("Clone workflow")
        expect(wrapper.findAll("button").at(1).text()).toBe("Cancel workflow")
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
    })

    // it(`it can render reports table`, async () => {
    //     const wrapper = getFullWrapper()
        
    //     await Vue.nextTick()
    //     await Vue.nextTick()
    //     await Vue.nextTick()
    //     const dropdown = wrapper.findComponent(vSelect)
    //     expect(dropdown.props("placeholder")).toBe("Search by name...")
    //     // await dropdown.trigger("click")
    //     // await Vue.nextTick()
    //     // await dropdown.find("input").setValue("name")
    //     // expect(dropdown.html()).toBe("Search by name...")
    //     await wrapper.setData({workflowRunStatus: workflowStatus1})
    //     await Vue.nextTick()
    //     expect(wrapper.html()).toBe("Search by name...")
    // })

    it(`it can render reports table`, async () => {
        const wrapper = getWrapper()
        
        await Vue.nextTick()
        await Vue.nextTick()
        await Vue.nextTick()
        await wrapper.setData({workflowRunStatus: workflowStatus1})
        expect(wrapper.find("table").exists()).toBe(true)
        expect(wrapper.findAll("tr").length).toBe(3)
        expect(wrapper.findAll("td > a").length).toBe(2)
        expect(wrapper.findAll("td > a").at(0).text()).toBe("report one a")
        expect(wrapper.findAll("td > a").at(1).text()).toBe("report two a")
        expect(wrapper.findAll("td > a").at(0).attributes("href")).toBe("http://app/run-report?report-name=report one a")
        expect(wrapper.findAll("td > a").at(1).attributes("href")).toBe("http://app/run-report?report-name=report two a")
        expect(wrapper.findAll("tr > td:nth-child(2)").at(0).text()).toBe("error")
        expect(wrapper.findAll("tr > td:nth-child(2)").at(0).attributes("style")).toBe("color: red;")
        expect(wrapper.findAll("tr > td:nth-child(2)").at(2).text()).toBe("running")
        expect(wrapper.findAll("tr > td:nth-child(2)").at(2).attributes("style")).toBe("color: grey;")
        expect(wrapper.findAll("tr > td:nth-child(3)").at(0).text()).toBe("Wed Jun 16 2021, 10:51")
    })

    it(`clicking successful report link sets run report tab in session`, async () => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;
        const wrapper = getWrapper()
        await Vue.nextTick()
        await Vue.nextTick()
        await Vue.nextTick()
        await wrapper.setData({workflowRunStatus: workflowStatus1})
        const link = wrapper.findAll("td > a").at(1)
        link.trigger("click")
        expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportTab");
        expect(spySetStorage.calls[0][1]).toBe("runReport");
    })

    it(`clicking an unsuccessful report link sets report logs tab in session`, async () => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;
        const wrapper = getWrapper()
        await Vue.nextTick()
        await Vue.nextTick()
        await Vue.nextTick()
        await wrapper.setData({workflowRunStatus: workflowStatus1})
        const link = wrapper.findAll("td > a").at(0)
        link.trigger("click")
        expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportTab");
        expect(spySetStorage.calls[0][1]).toBe("reportLogs");
    })
})