import {shallowMount} from "@vue/test-utils";
import runWorkflowProgress from '../../../js/components/runWorkflow/runWorkflowProgress.vue'
import {mockAxios} from "../../mockAxios";
import Vue from "vue";

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
          "date": "${Instant.now()}"
        },
        {
          "key": "hygienic_mammoth",
          "name": "report two a",
          "status": "success",
          "version": "20210510-100458-8f1a9624",
          "date": "${Instant.now()}"
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

    it(`it can render runWorkflowProgress page`, async () => {
        const wrapper = getWrapper()
        // wrapper.setData({workflowRunSummaries: workflows.data})
        // expect(wrapper.vm.$data.workflowRunSummaries).toBe("")
        
        await Vue.nextTick()
        expect(wrapper.text()).toBe("here")
        expect(wrapper.find("label").text()).toBe("Workflow")
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
    })
})