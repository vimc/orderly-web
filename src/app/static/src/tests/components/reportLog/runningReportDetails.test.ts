import Vue from "vue";
import {shallowMount} from "@vue/test-utils"
import runningReportsDetails from "../../../js/components/reportLog/runningReportDetails.vue"
import {mockAxios} from "../../mockAxios"
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {ReportLog} from "../../../js/utils/types";

describe(`runningReportDetails`, () => {

    const props = {
        reportKey: "half_aardwolf"
    }

    const initialReportLog: ReportLog = {
        email: "test@example.com",
        date: new Date(2021, 3, 21, 9, 26, 54).toISOString(),
        report: "minimal",
        instances: { "source": "support", "annexe" : "annexe_1"},
        params: {"name" : "nmin", "cologne" : "ey6"},
        git_branch: "branch value",
        git_commit: "commit value",
        status: "complete",
        logs: "some logs",
        report_version: "version"
    }

    const realSetTimeout = setTimeout;

    const getWrapper = (propsData = props, reportLog = initialReportLog) => {
        return shallowMount(runningReportsDetails,
            {
                propsData,
                data() {
                    return {
                        reportLog: reportLog
                    }
                }
            })
    }

    beforeEach(() => {
        mockAxios.reset();
        jest.useFakeTimers();
    });

    afterEach(() => {
        jest.runOnlyPendingTimers();
        jest.useRealTimers();
    });

    it("displays report name and date as expected", () => {
        const wrapper = getWrapper();

        const name = wrapper.find("#report-name");
        expect(name.findAll("span").at(0).text()).toBe("Report:");
        expect(name.findAll("span").at(1).text()).toBe("minimal");

        const start = wrapper.find("#report-start");
        expect(start.findAll("span").at(0).text()).toBe("Run started:");
        expect(start.findAll("span").at(1).text()).toBe("Wed Apr 21 2021, 09:26");
    });

    it("does not include report start section when date not in data", () => {
        const reportLog = {
            ...initialReportLog,
            date: null
        };
        const wrapper = getWrapper(props, reportLog);
        expect(wrapper.find("#report-start").exists()).toBe(false);
    });

    it(`displays git branch data as expected`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#report-log").find("#report-git-branch").exists()).toBeTruthy()
        const spans = wrapper.find("#report-git-branch").findAll("span")
        expect(spans.at(0).text()).toBe("Git branch:")
        expect(spans.at(1).text()).toBe("branch value")
    })

    it(`displays git commit data as expected`,  () => {
            const wrapper = getWrapper()
            expect(wrapper.find("#report-log").find("#report-git-commit").exists()).toBeTruthy()
            const spans = wrapper.find("#report-git-commit").findAll("span")
            expect(spans.at(0).text()).toBe("Git commit:")
            expect(spans.at(1).text()).toBe("commit value")
    })

    it(`displays parameter data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-params").findAll("span")
            expect(spans.at(0).text()).toBe("Parameters:")

            const table = wrapper.find("table")
            const rows = table.findAll("tr")
            const keyVal1 = rows.at(0).findAll("td")
            expect(keyVal1.at(0).text()).toBe("name")
            expect(keyVal1.at(1).text()).toBe("nmin")

            const keyVal2 = rows.at(1).findAll("td")
            expect(keyVal2.at(0).text()).toBe("cologne")
            expect(keyVal2.at(1).text()).toBe("ey6")
    })

    it(`displays database instances as expected`,  () => {
            const wrapper = getWrapper();
            const instances = wrapper.findAll("#report-database-instances .report-database-instance");

            expect(instances.length).toBe(2);
            expect(instances.at(0).findAll("span").at(0).text()).toBe("Database \"source\":");
            expect(instances.at(0).findAll("span").at(1).text()).toBe("support");
            expect(instances.at(1).findAll("span").at(0).text()).toBe("Database \"annexe\":");
            expect(instances.at(1).findAll("span").at(1).text()).toBe("annexe_1");
    });

    it(`displays no database instance row if there are no instances`, () => {
        let wrapper = getWrapper(props, {...initialReportLog, instances: null});
        expect(wrapper.find("#report-database-instances").exists()).toBe(false);

        wrapper = getWrapper(props, {...initialReportLog, instances: {}});
        expect(wrapper.find("#report-database-instances").exists()).toBe(false);
    });

    it(`displays status data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-status").findAll("span")
            expect(spans.at(0).text()).toBe("Status:")
            expect(spans.at(1).text()).toBe("complete")
    })

    it(`displays version data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-version").findAll("span")
            expect(spans.at(0).text()).toBe("Report version:")
            expect(spans.at(1).text()).toBe("version")
    })

    it(`displays Logs data as expected`,  () => {
            const wrapper = getWrapper()
            const textArea = wrapper.find("#report-logs").find("textarea")
            expect(textArea.text()).toBe("some logs")
    })

    it("sets logs textarea scrollTop to scrollHeight on getLogs", async () => {
        const key = "another aardwolf";
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": initialReportLog});

        const wrapper = getWrapper();
        const mockLogsRef = { scrollTop: 0, scrollHeight: 100 };
        (wrapper.vm as any).$refs.logs = mockLogsRef;

        await wrapper.setProps({reportKey: key});
        await Vue.nextTick();
        await Vue.nextTick();
        expect(mockLogsRef.scrollTop).toBe(100);
    });

    it(`does not displays data when report key in not given`, async (done) => {
        const key = ""
        const getWrapper = () => {
            return shallowMount(runningReportsDetails,
                {
                    propsData: {
                        reportKey: key
                    }
                })
        }
        const wrapper = getWrapper()

        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": initialReportLog});
        realSetTimeout(() => {
            expect(wrapper.find("#no-logs").exists()).toBe(true)
            expect(wrapper.find("#no-logs").text()).toBe("There are no logs to display")
            done()
        })
    })

    it(`it displays error message when report key in not valid`, (done) => {
        const key = "fakeKey"
        const getWrapper = () => {
            return shallowMount(runningReportsDetails,
                {
                    propsData: {
                        reportKey: key
                    }
                })
        }
        const wrapper = getWrapper()
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(500, "Error");

        realSetTimeout(() => {
            expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("Error")
            expect(wrapper.findComponent(ErrorInfo).props("defaultMessage"))
                .toBe("An error occurred when fetching logs")
            done()
        })
    })

    const testStartsPollingOnMountWhenIncomplete = (incompleteStatus: string, workflowKey: string | null, done) => {
        const key = `fakeKey-${incompleteStatus}`;
        const workflowParam = workflowKey ? `?workflow=${workflowKey}` : "";
        const url = `http://app/running/${key}/logs/${workflowParam}`;
        mockAxios.onGet(url)
            .reply(200, {"data": {...initialReportLog, status: incompleteStatus}});

        const wrapper = shallowMount(runningReportsDetails,
            {
                propsData: {
                    reportKey: key,
                    workflowKey
                }
            });

        realSetTimeout(() => {
            expect(mockAxios.history.get.filter(g => g.url === url).length).toBe(1);
            expect(wrapper.vm.$data.pollingTimer).not.toBeNull();
            expect(setInterval).toHaveBeenCalledTimes(1);
            expect(setInterval).toHaveBeenCalledWith((wrapper.vm as any).getLogs, 1500);

            //invoke the pending timer and expect getLogs to be invoked again - mockAxios should have been called a
            //second time
            jest.runOnlyPendingTimers();
            realSetTimeout(() => {
                expect(mockAxios.history.get.filter(g => g.url === url).length).toBe(2);
                done();
            }, 500);
        }, 500);
    };

    it(`starts polling on mount when report is running`,  (done) => {
        testStartsPollingOnMountWhenIncomplete("running", null,  done);
    });

    it(`starts polling on mount when report is queued`, (done) => {
        testStartsPollingOnMountWhenIncomplete("queued", null,  done);
    });

    it("polls with workflow parametr when provided", (done) => {
        testStartsPollingOnMountWhenIncomplete("running", "test-workflow", done);
    });

    it("does not start or stop polling on mount when report is complete", (done) => {
        const key = "fakeKey";
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": initialReportLog});

        const wrapper = shallowMount(runningReportsDetails,
            {
                propsData: {
                    reportKey: key
                }
            });

        realSetTimeout(() => {
            expect(setInterval).toHaveBeenCalledTimes(0);
            expect(clearInterval).toHaveBeenCalledTimes(0);
            done();
        });
    });

    it("getLogs does not start polling on incomplete report if pollingTimer is already set", (done) => {
        const key = "fakeKey";
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": {...initialReportLog, status: "queued"}});

        const wrapper = shallowMount(runningReportsDetails,
            {
                propsData: {
                    reportKey: key
                },
                data() {
                    return {pollingTimer: 123}
                }
            });

        realSetTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.vm.$data.pollingTimer).toBe(123);
            expect(setInterval).toHaveBeenCalledTimes(0);
            done();
        });
    });

    it("getLogs stops polling on complete report", (done) => {
        const key = "fakeKey";
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": initialReportLog});

        const wrapper = shallowMount(runningReportsDetails,
            {
                propsData: {
                    reportKey: key
                },
                data() {
                    return {pollingTimer: 123}
                }
            });

        realSetTimeout(() => {
            expect(clearInterval).toHaveBeenCalledTimes(1);
            expect(clearInterval).toHaveBeenCalledWith(123);
            expect(setInterval).toHaveBeenCalledTimes(0);
            expect(wrapper.vm.$data.pollingTimer).toBe(null);
            done();
        });

    });

    it("stops polling and gets logs, starts polling for new key when reportKey changes", () => {
        const oldKey = "fakeKey";
        const newKey = "newKakeKey";
        mockAxios.onGet(`http://app/running/${oldKey}/logs/`)
            .reply(200, {"data": {...initialReportLog, status: "queued"}});
        mockAxios.onGet(`http://app/running/${newKey}/logs/`)
            .reply(200, {"data": {...initialReportLog, status: "running"}});

        const wrapper = shallowMount(runningReportsDetails,
            {
                propsData: {
                    reportKey: oldKey
                },
                data() {
                    return {pollingTimer: 123}
                }
            });

        realSetTimeout(() => {
            expect(mockAxios.history.get[0].url).toBe(`http://app/running/${oldKey}/logs/`);
            expect(clearInterval).toHaveBeenCalledTimes(0);
            expect(setInterval).toHaveBeenCalledTimes(0);

            wrapper.setProps({reportKey: newKey});

            realSetTimeout(() => {
                expect(clearInterval).toHaveBeenCalledTimes(1);
                expect(clearInterval).toHaveBeenCalledWith(123);
                expect(mockAxios.history.get[1].url).toBe(`http://app/running/${newKey}/logs/`)
                expect(setInterval).toHaveBeenCalledTimes(1);
                expect(wrapper.vm.$data.pollingTimer).not.toBe(123);
                expect(wrapper.vm.$data.pollingTimer).not.toBe(null);
            });
        });
    });
});
