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

    it(`it can render if no workflows returned`, async (done) => {
        mockAxios.onGet('http://app/workflows')
            .reply(200, null);
        const wrapper = getWrapper()
        
        setTimeout(() => {
            expect(wrapper.find("p").text()).toBe("No workflows to show")
            done();
        })
    })

    it(`it can render runWorkflowProgress page`, async (done) => {
        const wrapper = getWrapper()
        
        setTimeout(() => {
            expect(wrapper.find("label").text()).toBe("Workflow")
            expect(wrapper.find("v-select-stub").attributes("placeholder")).toBe("Select workflow or search by name...")
            // Tests to be reinstated once buttons are integrated with workflow wizard
            // expect(wrapper.findAll("button").at(0).text()).toBe("Clone workflow")
            // expect(wrapper.findAll("button").at(1).text()).toBe("Cancel workflow")
            done();
        })
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
    })

    it(`it can render reports table`, async (done) => {
        const wrapper = getWrapper()
        
        setTimeout(() => {
            wrapper.setData({workflowRunStatus: workflowStatus1})
            setTimeout(() => {
                expect(wrapper.find("table").exists()).toBe(true)
                expect(wrapper.findAll("tr").length).toBe(3)
                const reportLinks = wrapper.findAll("td > a")
                expect(reportLinks.length).toBe(2)

                const errorReportLink = reportLinks.at(0)
                expect(errorReportLink.text()).toBe("report one a")
                expect(errorReportLink.attributes("href")).toBe("http://app/run-report?report-name=report one a")

                const completedReportLink = reportLinks.at(1)
                expect(completedReportLink.text()).toBe("report two a")
                expect(completedReportLink.attributes("href")).toBe("http://app/report/report two a/20210510-100458-8f1a9624/")

                const errorStatus = wrapper.findAll("tr > td:nth-child(2)").at(0)
                expect(errorStatus.text()).toBe("Failed")
                expect(errorStatus.classes()).toContain("text-danger")

                const successStatus = wrapper.findAll("tr > td:nth-child(2)").at(1)
                expect(successStatus.text()).toBe("Complete")

                const runningStatus = wrapper.findAll("tr > td:nth-child(2)").at(2)
                expect(runningStatus.text()).toBe("Running")
                expect(runningStatus.classes()).toContain("text-secondary")

                const dateColumns = wrapper.findAll("tr > td:nth-child(3)")
                expect(dateColumns.at(0).text()).toBe("Wed Jun 16 2021, 09:51")
                done();
            })
        })
    })

    it(`clicking an unsuccessful report link sets report logs tab in session`, async (done) => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;
        const wrapper = getWrapper()
        setTimeout(() => {
            wrapper.setData({workflowRunStatus: workflowStatus1})
            setTimeout(() => {
                const link = wrapper.findAll("td > a").at(0)
                link.trigger("click")
                expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportTab");
                expect(spySetStorage.calls[0][1]).toBe("reportLogs");
                done();
            })
        });
    })
})