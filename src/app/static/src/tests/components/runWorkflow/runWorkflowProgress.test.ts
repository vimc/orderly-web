import {shallowMount} from "@vue/test-utils";
import runWorkflowProgress from '../../../js/components/runWorkflow/runWorkflowProgress.vue'
import {mockAxios} from "../../mockAxios";

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
            // Tests to be reinstated once buttons are unhidden and made active by mrc-2513
            // expect(wrapper.findAll("button").at(0).text()).toBe("Clone workflow")
            // expect(wrapper.findAll("button").at(1).text()).toBe("Cancel workflow")
            expect(wrapper.findAll("button").length).toBe(0)
            done();
        })
    })

    it(`it can render reports table`, async (done) => {
        const wrapper = getWrapper()
        
        setTimeout(() => {
            wrapper.setData({workflowRunStatus: workflowStatus1.data})
            setTimeout(() => {
                expect(wrapper.find("table").exists()).toBe(true)
                expect(wrapper.findAll("tr").length).toBe(3)
                const reportLinks = wrapper.findAll("td > a")
                expect(reportLinks.length).toBe(1)

                const completedReportLink = reportLinks.at(0)
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

    it(`can fetch workflow details and emit rerun event`, (done) => {
        const workflowDetails = {name: "Test Workflow", reports: []};
        mockAxios.onGet('http://app/workflows/test-key/')
            .reply(200, {data: workflowDetails});

        const wrapper = shallowMount(runWorkflowProgress, {
            data: () => {
            return {
                    selectedWorkflowKey: "test-key",
                    workflowRunSummaries: []
                };
            }
        });

        wrapper.find("#rerun").trigger("click");
        setTimeout(() => {
            expect(wrapper.emitted("rerun")[0][0]).toStrictEqual(workflowDetails);
            done();
        });
    });
})
