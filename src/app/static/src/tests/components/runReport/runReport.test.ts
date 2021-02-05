import Vue from "vue";
import {shallowMount, mount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";
import ReportList from "../../../js/components/runReport/reportList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";
import ParameterList from "../../../js/components/runReport/parameterList.vue";

describe("runReport", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(200, {"data": gitCommits});
    });

    const gitCommits = [
        {id: "abcdef", date_time: "Mon Jun 08, 12:01"},
        {id: "abc123", date_time: "Tue Jun 09, 13:11"}
    ];

    const mockParams = [
        {name: "global", default: "test"},
        {name: "minimal", default: "random_39id"}
    ]

    const gitBranches = ["master", "dev"];

    const props = {
        metadata: {
            git_supported: true,
            instances_supported: false
        },
        gitBranches
    };

    const reports = [
        {name: "report1", date: new Date().toISOString()},
        {name: "report2", date: null}
    ];

    const getWrapper = (report = reports, propsData = props) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": reports});

        return mount(RunReport, {
            propsData
        });
    }

    it("renders git branch drop down and fetches commits if git supported", (done) => {

        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);
        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master");
        expect(options.at(0).attributes().value).toBe("master");
        expect(options.at(1).text()).toBe("dev");
        expect(options.at(1).attributes().value).toBe("dev");

        setTimeout(() => {
            expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
            const commitOptions = wrapper.findAll("#git-commit option");
            expect(commitOptions.length).toBe(2);
            expect(commitOptions.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
            expect(commitOptions.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

            expect(wrapper.vm.$data.selectedCommitId).toBe("abcdef");
            done();
        })
    });

    it("does not render git drop downs if git not supported", async () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: false, instances_supported: false},
                gitBranches: null
            }
        });

        await Vue.nextTick();
        expect(mockAxios.history.get.length).toBe(1);
        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });

    it("calls api to get commits when branch changes and updates commits drop down", (done) => {
        mockAxios.onGet('http://app/git/branch/dev/commits/')
            .reply(200, {"data": gitCommits});
        mockAxios.onGet('http://app/reports/runnable/?branch=dev&commit=abcdef')
            .reply(200, {"data": []});

        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        wrapper.findAll("#git-branch option").at(1).setSelected();

        expect(wrapper.vm.$data.selectedBranch).toBe("dev");

        setTimeout(() => {
            const options = wrapper.findAll("#git-commit option");
            expect(options.length).toBe(2);
            expect(options.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
            expect(options.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

            expect(wrapper.vm.$data.selectedCommitId).toBe("abcdef");

            expect(wrapper.find(ErrorInfo).props("apiError")).toBe("");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("");
            done();
        })
    });

    it("show error message if error getting git commits", (done) => {
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching Git commits");
            done();
        })
    });

    it("updates reports dropdown by calling api when commit changes", (done) => {
        const wrapper = getWrapper(reports);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(ErrorInfo).props("apiError")).toBe("");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("");
            expect(wrapper.find(ReportList).props("reports")).toEqual(expect.arrayContaining(reports));
            done();
        });
    });

    it("displays report list in order and allows selection and reset", (done) => {
        const wrapper = getWrapper(reports);

        setTimeout(async () => {
            wrapper.find(ReportList).find("a:last-of-type").trigger("click");
            expect(wrapper.vm.$data["selectedReport"]).toBe("report2");
            await Vue.nextTick();
            wrapper.find(ReportList).find("button").trigger("click");
            expect(wrapper.vm.$data["selectedReport"]).toBe("");
            done();
        });
    });

    it("shows instances if instances supported", () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: false,
                    instances_supported: true,
                    instances: {
                        source: ["prod", "uat"],
                        annex: ["one"],
                        another: []
                    }
                },
                gitBranches
            },
            data() {
                return {
                    selectedReport: "report1"
                }
            }
        });

        const sourceOptions = wrapper.findAll("#source option");
        expect(sourceOptions.length).toBe(2);
        expect(sourceOptions.at(0).attributes().value).toBe("prod");
        expect(sourceOptions.at(0).text()).toBe("prod");
        expect(sourceOptions.at(1).attributes().value).toBe("uat");
        expect(sourceOptions.at(1).text()).toBe("uat");

        expect(wrapper.find("#annex").exists()).toBe(false); // only 1 option so don't show
        expect(wrapper.find("#another").exists()).toBe(false); // no options so don't show
    });

    it("doesn't show instances if instances not supported", () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    instances_supported: false,
                    instances: {
                        source: ["prod", "uat"],
                        annex: ["one"],
                        another: []
                    }
                },
                gitBranches
            }
        });

        expect(wrapper.find("#source").exists()).toBe(false);
        expect(wrapper.find("#annex").exists()).toBe(false);
        expect(wrapper.find("#another").exists()).toBe(false);
    });

    it("it does render parameters control correctly if report is selected and param has data", () => {
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                gitBranches
            },
            data() {
                return {
                    gitCommits: gitCommits,
                    parameterValues: mockParams,
                    selectedReport: "reports"
                }
            }
        });

        expect(wrapper.find("#parameters").exists()).toBe(true);
        const labels = wrapper.find(ParameterList).findAll("label")
        expect(labels.at(0).text()).toBe("global");
        expect(labels.at(1).text()).toBe("minimal");

        const inputs = wrapper.find(ParameterList).findAll("input")
        expect(inputs.length).toBe(2);

    });

    it("does not render parameters control if report is not selected", () => {
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                gitBranches
            },
            data() {
                return {
                    gitCommits: gitCommits,
                    parameterValues: [],
                    selectedReport: "reports"
                }
            }
        });
        expect(wrapper.find("#parameters").exists()).toBe(false);
        expect(wrapper.find(ParameterList).exists()).toBe(false);
    });

    it("parameters endpoint can get data successfully", async (done) => {
        const mockAxiosParam = [{name: "minimal", default: "random_39id"}]
        const url = "http://app/reports/minimal/parameters/?commit=abcdef"

        mockAxios.onGet(url)
            .reply(200, {"data": mockAxiosParam});

        const wrapper = getWrapper()
        setTimeout(async () => {
            wrapper.setData({
                selectedReport: "minimal",
                error: "test-error",
                defaultMessage: "test-msg",
                parameterValues: []
            });
            await Vue.nextTick();

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe(url);
                expect(wrapper.find("#parameters").exists()).toBe(true);
                expect(wrapper.vm.$data.parameterValues).toMatchObject(mockAxiosParam);
                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");
                done();
            });
        });
    })

    it("parameters endpoint can set defaultmessage when errored", (done) => {
        const url = "http://app/reports/minimal/parameters/?commit=test-commit"
        mockAxios.onGet(url)
            .reply(500, "Parameter fetching error");

        const wrapper = getWrapper();

        setTimeout(async () => {
            wrapper.setData({
                selectedReport: "minimal",
                selectedCommitId: "test-commit",
                error: "",
                defaultMessage: ""
            });

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe(url);
                expect(wrapper.find("#parameters").exists()).toBe(false);
                expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred when getting parameters");
                done();
            });
        });
    });

    it("does not render parameters control if parameters and selected report data do not exist", () => {
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                gitBranches
            },
            data() {
                return {
                    gitCommits: gitCommits,
                    parameterValues: [],
                    selectedReport: ""
                }
            }
        });
        expect(wrapper.find("#parameters").exists()).toBe(false);
        expect(wrapper.find(ParameterList).exists()).toBe(false);
    });

    it("renders run button group if there is a selected report", async () => {
        const wrapper = getWrapper();
        wrapper.setData({selectedReport: "test-report"});
        await Vue.nextTick();
        const runGroup = wrapper.find("#run-form-group");
        expect(runGroup.exists()).toBe(true);
        expect(runGroup.find("button").text()).toBe("Run report");
        expect(runGroup.find("button").attributes("disabled")).toBeUndefined();
        expect(runGroup.find("run-report-status").exists()).toBe(false);
    });

    it("does not render run button group if there is no selected report", () => {
        const wrapper = getWrapper();
        const runGroup = wrapper.find("#run-form-group");
        expect(runGroup.exists()).toBe(false);
    });

    it("clicking run button sends run request and displays status on success", async (done) => {
        const param_url = "http://app/reports/test-report/parameters/?commit=test-commit"
        mockAxios.onGet(param_url)
            .reply(200, {"data": []});

        const url = 'http://app/report/test-report/actions/run/';
        mockAxios.onPost(url, {})
            .reply(200, {data: {key: "test-key"}});

        const propsData = {
            metadata: {
                git_supported: true,
                instances_supported: true,
                instances: {
                    annexe: ["a1", "a2"],
                    source: ["uat", "science", "prod"]
                }
            },
            gitBranches
        };
        const wrapper = getWrapper(reports, propsData);

        setTimeout(async () => { //give the wrapper time to fetch reports
            wrapper.setData({
                selectedReport: "test-report",
                selectedCommitId: "test-commit",
                selectedInstances: {source: "science", annexe: "a1"},
                error: "test-error",
                defaultMessage: "test-msg"
            });

            await Vue.nextTick();
            wrapper.find("#run-form-group button").trigger("click");

            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe(param_url);
                expect(mockAxios.history.post[0].url).toBe(url);
                expect(mockAxios.history.post[0].params).toStrictEqual({ref: "test-commit", instance: "science"});
                expect(wrapper.find("#run-report-status").text()).toContain("Run started");
                expect(wrapper.find("#run-report-status a").text()).toBe("Check status");
                expect(wrapper.find("#run-form-group button").attributes("disabled")).toBe("disabled");
                expect(wrapper.vm.$data.runningKey).toBe("test-key");
                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");
                done();
            });
        });
    });

    it("clicking run button sends run request and sets error", async (done) => {
        const url = 'http://app/report/test-report/actions/run/';
        mockAxios.onPost(url, {})
            .reply(500, "TEST ERROR");
        const wrapper = getWrapper();

        setTimeout(async () => { //give the wrapper time to fetch reports
            wrapper.setData({
                selectedReport: "test-report",
                selectedCommitId: "test-commit",
                error: "",
                defaultMessage: ""
            });

            await Vue.nextTick();
            wrapper.find("#run-form-group button").trigger("click");

            setTimeout(() => {
                expect(wrapper.find("#run-report-status").exists()).toBe(false);
                expect(wrapper.vm.$data.runningKey).toBe("");
                expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
                expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred when running report");
                done();
            });
        });
    });

    it("clicking 'Check status' sends status request and displays status on success, and resets disableRun", async (done) => {
        const url = 'http://app/report/test-report/actions/status/test-key/';
        mockAxios.onGet(url)
            .reply(200, {data: {status: "test-status"}});
        const wrapper = getWrapper();

        setTimeout(async () => { //give the wrapper time to fetch reports
            wrapper.setData({
                selectedReport: "test-report",
                error: "test-error",
                defaultMessage: "test-msg"
            });
            await Vue.nextTick();

            //Set data in two stages because status and key get reset by watch on selectedReport change
            wrapper.setData({
                runningStatus: "Run started",
                runningKey: "test-key",
                disableRun: true
            });
            await Vue.nextTick();

            expect(wrapper.find("#run-form-group button").attributes("disabled")).toBe("disabled");
            wrapper.find("#run-form-group a").trigger("click");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(4);
                expect(mockAxios.history.get[3].url).toBe(url);

                expect(wrapper.find("#run-report-status").text()).toContain("Running status: test-status");
                expect(wrapper.find("#run-report-status a").text()).toBe("Check status");
                expect(wrapper.find("#run-form-group button").attributes("disabled")).toBeUndefined();
                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");
                done();
            });
        });
    });

    it("clicking 'Check status' sends status request and displays error", async (done) => {
        const url = 'http://app/report/test-report/actions/status/test-key/';
        mockAxios.onGet(url)
            .reply(500, "TEST ERROR");
        const wrapper = getWrapper();

        setTimeout(async () => { //give the wrapper time to fetch reports
            wrapper.setData({
                selectedReport: "test-report",
                error: "test-error",
                defaultMessage: "test-msg"
            });
            await Vue.nextTick();

            //Set data in two stages because runningStatus gets reset by watch on selectedReport change
            wrapper.setData({
                runningKey: "test-key",
                runningStatus: "Run started"
            });
            await Vue.nextTick();

            wrapper.find("#run-form-group a").trigger("click");

            setTimeout(() => {
                expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
                expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred when fetching report status");
                done();
            });
        });
    });

    it("changing selectedReport resets runningStatus and enables run", async () => {
        const wrapper = getWrapper();
        wrapper.setData({selectedReport: "previous-report"});
        await Vue.nextTick();

        wrapper.setData({runningStatus: "test-status", disableRun: true});
        await Vue.nextTick();
        expect(wrapper.vm.$data.runningStatus).toBe("test-status");
        expect(wrapper.find("#run-form-group button").attributes("disabled")).toBe("disabled");

        wrapper.setData({selectedReport: "test-report"});
        await Vue.nextTick();
        expect(wrapper.vm.$data.runningStatus).toBe("");
        expect(wrapper.find("#run-form-group button").attributes("disabled")).toBeUndefined();
    });

    it("changing a selected instance updates data and resets runningStatus and disabledRun", async () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    instances_supported: true,
                    instances: {
                        source: ["prod", "uat"],
                    }
                },
                gitBranches
            }
        });
        wrapper.setData({selectedReport: "test-report"});
        await Vue.nextTick();
        wrapper.setData({runningStatus: "test-status", disableRun: true});
        await Vue.nextTick();
        expect(wrapper.vm.$data.runningStatus).toBe("test-status");
        expect(wrapper.vm.$data.selectedInstances).toStrictEqual({source: "prod"});
        expect(wrapper.find("#run-form-group button").attributes("disabled")).toBe("disabled");

        const select = wrapper.find("#source");
        select.setValue("uat");
        await Vue.nextTick();

        expect(wrapper.vm.$data.selectedInstances).toStrictEqual({source: "uat"});
        expect(wrapper.vm.$data.runningStatus).toBe("");
        expect(wrapper.find("#run-form-group button").attributes("disabled")).toBeUndefined();
    });
});
