import {shallowMount} from "@vue/test-utils";
import runWorkflowProgress from '../../../js/components/runWorkflow/runWorkflowProgress.vue'
import WorkflowReportLogDialog from "../../../js/components/runWorkflow/workflowReportLogDialog.vue";
import {mockAxios} from "../../mockAxios";
import errorInfo from "../../../js/components/errorInfo.vue";

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
        },
        {
            "key": "non_hygienic_mammoth",
            "name": "report four a",
            "status": "impossible",
            "date": "2021-06-16T09:51:16Z"
          },
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

    afterEach(() => {
        jest.useRealTimers()
    })

    const getWrapper = (initialSelectedWorkflow = "") => {
        return shallowMount(runWorkflowProgress, {propsData: {workflowMetadata: {}, initialSelectedWorkflow}})
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
            expect(wrapper.find("v-select-stub").props("clearable")).toBe(false)
            expect(wrapper.findAll("button").length).toBe(0)
            done();
        })
    })

    it(`initial selected workflow is set by props and emitted`, async (done) => {
        const wrapper = getWrapper("test")
        setTimeout(() => {
            expect(wrapper.vm.$data.selectedWorkflowKey).toBe("test")
            expect(wrapper.emitted("set-selected-workflow-key")).toStrictEqual([["test"]])
            done();
        })
    })

    it(`changes to selected workflow are emitted`, async (done) => {
        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "test"})
        setTimeout(() => {
            expect(wrapper.emitted("set-selected-workflow-key")).toStrictEqual([["test"]])
            done();
        })
    })

    it(`renders get workflows error message`, async (done) => {
        mockAxios.reset();
        mockAxios.onGet('http://app/workflows')
            .reply(500, "TEST ERROR");
        const wrapper = getWrapper()

        setTimeout(() => {
            expect(wrapper.find("error-info-stub").props("defaultMessage")).toBe("An error occurred fetching the workflows")
            expect(wrapper.find("error-info-stub").props("apiError")).toBeTruthy()
            expect(wrapper.vm.$data.error.response.data).toStrictEqual("TEST ERROR")
            expect(wrapper.vm.$data.workflowRunSummaries).toStrictEqual(null)
            done();
        })
    })

    it(`renders get workflow run status error message`, async (done) => {
        mockAxios.reset();
        mockAxios.onGet('http://app/workflows/key1/status')
            .reply(500, "TEST ERROR");
        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "key1"})

        setTimeout(() => {
            expect(wrapper.find("error-info-stub").props("defaultMessage")).toBe("An error occurred fetching the workflow reports")
            expect(wrapper.find("error-info-stub").props("apiError")).toBeTruthy()
            expect(wrapper.vm.$data.error.response.data).toStrictEqual("TEST ERROR")
            expect(wrapper.vm.$data.workflowRunStatus).toStrictEqual(null)
            done();
        })
    })

    it(`it can render reports table`, (done) => {
        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "key1"})

        setTimeout(() => {
            expect(wrapper.find("table").exists()).toBe(true)
            expect(wrapper.findAll("tr").length).toBe(4)
            const reportLinks = wrapper.findAll("td > a.report-version-link")
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

            const dependencyErrorStatus = wrapper.findAll("tr > td:nth-child(2)").at(3)
            expect(dependencyErrorStatus.text()).toBe("Dependency failed")
            expect(dependencyErrorStatus.classes()).toContain("text-danger")

            const dateColumns = wrapper.findAll("tr > td:nth-child(3)")
            expect(dateColumns.at(0).text()).toBe("Wed Jun 16 2021, 09:51")

            const logCells = wrapper.findAll("tr > td:nth-child(4)");
            logCells.wrappers.forEach(td => {
                const link = td.find("a.report-log-link");
                expect(link.text()).toBe("View log");
                expect(link.attributes("href")).toBe("#");
            });
            done();
        })
    });

    it("renders report log dialog", async () => {
        const wrapper = getWrapper();
        await wrapper.setData({selectedWorkflowKey: "key1"});
        const dialog = wrapper.find(WorkflowReportLogDialog);
        expect(dialog.props("reportKey")).toBe(null);
        expect(dialog.props("workflowKey")).toBe("key1");
    });

    it(`sets report log dialog report key on click View log link`, (done) => {
        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "key1"})

        setTimeout(async () => {
            const link = wrapper.findAll("tr").at(0).find("a.report-log-link");
            await link.trigger("click");

            const dialog = wrapper.find(WorkflowReportLogDialog);
            expect(dialog.props("reportKey")).toBe("preterrestrial_andeancockoftherock");

            done();
        })
    });

    it("resets report log dialog report key when dialog emits close event", (done) => {
        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "key1"})

        setTimeout(async () => {
            const link = wrapper.findAll("tr").at(0).find("a.report-log-link");
            await link.trigger("click");

            const dialog = wrapper.find(WorkflowReportLogDialog);
            await dialog.vm.$emit("close");

            expect(dialog.props("reportKey")).toBeNull();

            done();
        })
    });

    it(`can fetch workflow details and emit rerun event`, (done) => {
        const workflowDetails = {
            name: "Test Workflow",
            key: "curious_mongoose",
            email: "test.user@example.com",
            date: "2021-08-01",
            instances: {source: "UAT"},
            git_branch: "master",
            git_commit: null,
            reports: [
                {
                    workflow_key: "curious_mongoose",
                    key: "terrified_ocelot",
                    report: "report1",
                    params: {p1: "v1" }
                },
                {
                    workflow_key: "curious_mongoose",
                    key: "weird_anteater",
                    report: "report2",
                    params: {}
                }
            ]
        };
        mockAxios.onGet("http://app/workflows/test-key/")
            .reply(200, {data: workflowDetails});

        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "test-key"})

        setTimeout(() => {
            const rerunButton = wrapper.find("#rerun");
            expect(rerunButton.text()).toBe("Re-run workflow");
            rerunButton.trigger("click");

            const expectedWorkflowMetadata = {
                name: "Test Workflow",
                instances: {source: "UAT"},
                git_branch: "master",
                git_commit: null,
                changelog: null,
                reports: [
                    {
                        name: "report1",
                        params: {p1: "v1" }
                    },
                    {
                        name: "report2",
                        params: {}
                    }
                ]
            };
            setTimeout(() => {
                expect(wrapper.emitted("rerun")[0][0]).toStrictEqual(expectedWorkflowMetadata);
                done();
            });
        });
    });

    it(`sets error when fail to fetch workflow details`, (done) => {
        mockAxios.onGet("http://app/workflows/test-key/")
            .reply(500, "TEST ERROR");
        const wrapper = getWrapper()
        wrapper.setData({selectedWorkflowKey: "test-key"})
        setTimeout(() => {
            wrapper.find("#rerun").trigger("click");
            setTimeout(() => {
                expect(wrapper.findComponent(errorInfo).props("apiError").response.data).toBe("TEST ERROR");
                expect(wrapper.findComponent(errorInfo).props("defaultMessage")).toBe("An error occurred fetching workflow details");
                expect(wrapper.emitted("rerun")).toBeUndefined();
                done();
            });
        });
    });

    it(`does start polling when workflow key is selected`, async (done) => {
        const key = "fakeKey";
        const wrapper = getWrapper()
        const realSetTimeout = setTimeout;
        jest.useFakeTimers();

        await wrapper.setData({selectedWorkflowKey: key})

        realSetTimeout(() => {
            expect(mockAxios.history.get[1].url).toBe(`http://app/workflows/${key}/status`)
            expect(wrapper.vm.$data.pollingTimer).not.toBe(null);
            expect(setInterval).toHaveBeenCalledTimes(1);
            expect(clearInterval).toHaveBeenCalledTimes(0);
            done();
        });
    })

    it(`does not start new polling when polling is currently running`, async (done) => {
        const key = "fakeKey";
        const key2 = "fakeKey2";

        const workflowStatusComplete = {
            "status": "success",
            "errors": null,
            "data": {
                "status": "complete",
                "reports": [
                    {
                        "key": "preterrestrial_andeancockoftherock",
                        "name": "report one a",
                        "status": "success",
                        "date": "2021-06-16T09:51:16Z"
                    }
                ]
            }
        }

        mockAxios.onGet(`http://app/workflows/${key2}/status`)
            .reply(200, workflowStatusComplete);

        const wrapper = getWrapper()
        const realSetTimeout = setTimeout;
        jest.useFakeTimers();

        await wrapper.setData({selectedWorkflowKey: key, pollingTimer: 100})

        realSetTimeout(async() => {
            expect(mockAxios.history.get[1].url).toBe(`http://app/workflows/${key}/status`)
            expect(wrapper.vm.$data.pollingTimer).toBe(100);
            expect(setInterval).toHaveBeenCalledTimes(0);
            expect(clearInterval).toHaveBeenCalledTimes(0);

            await wrapper.setData({selectedWorkflowKey: key2})
            expect(mockAxios.history.get[2].url).toBe(`http://app/workflows/${key2}/status`)
            expect(wrapper.vm.$data.pollingTimer).toBe(100);
            expect(setInterval).toHaveBeenCalledTimes(0);
            expect(clearInterval).toHaveBeenCalledTimes(0);
            done();
        });
    })


    it(`can stop polling when workflow run is complete`, async (done) => {
        const key = "fakeKey";
        const workflowStatusComplete = {
            "status": "success",
            "errors": null,
            "data": {
                "status": "complete",
                "reports": [
                    {
                        "key": "preterrestrial_andeancockoftherock",
                        "name": "report one a",
                        "status": "success",
                        "date": "2021-06-16T09:51:16Z"
                    }
                ]
            }
        }

        mockAxios.onGet(`http://app/workflows/${key}/status`)
            .reply(200, workflowStatusComplete);

        const wrapper = getWrapper()
        const realSetTimeout = setTimeout;
        jest.useFakeTimers();

        await wrapper.setData({selectedWorkflowKey: key})

        realSetTimeout(() => {
            expect(mockAxios.history.get[1].url).toBe(`http://app/workflows/${key}/status`)
            expect(wrapper.vm.$data.pollingTimer).toBe(null);
            expect(setInterval).toHaveBeenCalledTimes(1);
            expect(clearInterval).toHaveBeenCalledTimes(1);
            done();
        });
    })

    it(`it does not start polling when workflow is not selected`, async (done) => {
        const wrapper = getWrapper()

        const realSetTimeout = setTimeout;
        jest.useFakeTimers();

        realSetTimeout(() => {
            expect(wrapper.vm.$data.pollingTimer).toBe(null);
            expect(setInterval).toHaveBeenCalledTimes(0);
            expect(clearInterval).toHaveBeenCalledTimes(0);
            done();
        });
    })

})
