import Vue from "vue";
import {mount, shallowMount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";
import ReportList from "../../../js/components/runReport/reportList.vue";
import GitUpdateReports from "../../../js/components/runReport/gitUpdateReports.vue";
import {mockAxios} from "../../mockAxios";
import ParameterList from "../../../js/components/runReport/parameterList.vue";
import Instances from "../../../js/components/runReport/instances.vue";
import changeLog from "../../../js/components/runReport/changeLog.vue";

describe("runReport", () => {
    const mockParams = [
        {name: "global", value: "test"},
        {name: "minimal", value: "random_39id"}
    ]

    const initialGitBranches = ["master", "dev"];

    const props = {
        metadata: {
            git_supported: true,
            instances_supported: false
        },
        initialGitBranches,
        initialReportName: ""
    };

    const reports = [
        {name: "report1", date: new Date().toISOString()},
        {name: "report2", date: null}
    ];

    const getWrapper = (propsData = props) => {
        return shallowMount(RunReport, {
            propsData,
            data() {
                return {
                    reports
                }
            }
        });
    };

    beforeEach(() => {
        mockAxios.reset();
    });

    it("renders header", () => {

        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                initialGitBranches
            }
        });

        expect(wrapper.find("h2").text()).toBe("Run a report");
    });

    it("renders gitUpdateReports component", () => {
        const wrapper = getWrapper();
        const gitUpdateReports = wrapper.findComponent(GitUpdateReports);
        expect(gitUpdateReports.props("reportMetadata")).toBe(props.metadata);
        expect(gitUpdateReports.props("initialBranches")).toBe(initialGitBranches);
    });

    it("selects branch when event emitted from gitUpdateReports", async () => {
        const wrapper = getWrapper();
        const gitUpdateReports = wrapper.findComponent(GitUpdateReports);
        gitUpdateReports.vm.$emit("branchSelected", "dev");
        await Vue.nextTick();
        expect(wrapper.vm.$data["selectedBranch"]).toBe("dev");
    });

    it("selects commit when event emitted from gitUpdateReports", async () => {
        const wrapper = getWrapper();
        const gitUpdateReports = wrapper.findComponent(GitUpdateReports);
        gitUpdateReports.vm.$emit("commitSelected", "abc123");
        await Vue.nextTick();
        expect(wrapper.vm.$data["selectedCommitId"]).toBe("abc123");
    });

    it("updates reports when event emitted from gitUpdateReports", async () => {
        const wrapper = getWrapper();
        const gitUpdateReports = wrapper.findComponent(GitUpdateReports);
        const newReports = [{name: "report3", date: new Date().toISOString()}];
        gitUpdateReports.vm.$emit("reportsUpdate", newReports);
        await Vue.nextTick();
        expect(wrapper.vm.$data["reports"]).toBe(newReports);
    });

    it("displays report list in order and allows selection and reset", async () => {
        const wrapper = mount(RunReport, {
            propsData: props,
            data() {
                return {
                    reports
                }
            }
        });
        wrapper.find(ReportList).find("a:last-of-type").trigger("click");
        expect(wrapper.vm.$data["selectedReport"]).toBe("report2");
        await Vue.nextTick();
        wrapper.find(ReportList).find("button").trigger("click");
        expect(wrapper.vm.$data["selectedReport"]).toBe("");
    });

    it("report list shows initialReportName prop", () => {

        const wrapper = getWrapper({
            ...props,
            initialReportName: "minimal"
        });
        expect(wrapper.find(ReportList).props("initialSelectedReport")).toBe("minimal")
    });

    it("shows instances if instances supported", async() => {
        const wrapper = mount(RunReport, {
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
                initialGitBranches
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
                initialGitBranches
            }
        });

        expect(wrapper.find("#source").exists()).toBe(false);
        expect(wrapper.find("#annex").exists()).toBe(false);
        expect(wrapper.find("#another").exists()).toBe(false);
    });

    it("it sets parameters correctly when input change if report is selected and param has data", () => {
        const expectedParams = [
            {name: "global", value: "Set new value"},
            {name: "minimal", value: "Set new value 2"}
        ];

        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                initialGitBranches
            },
            data() {
                return {
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
        inputs.at(0).setValue("Set new value");
        inputs.at(1).setValue("Set new value 2");

        const newValues = (wrapper.vm as any).parameterValues;
        expect(newValues).toStrictEqual(expectedParams);
    });

    it("does not render parameters control if report is not selected", () => {
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                initialGitBranches
            },
            data() {
                return {
                    parameterValues: [],
                    selectedReport: "reports"
                }
            }
        });
        expect(wrapper.find("#parameters").exists()).toBe(false);
        expect(wrapper.find(ParameterList).exists()).toBe(false);
    });

    it("parameters endpoint can get data successfully", (done) => {
        const mockAxiosParam = [{name: "minimal", value: "random_39id"}];
        const url = "http://app/report/minimal/config/parameters/?commit=abcdef";

        mockAxios.onGet(url)
            .reply(200, {"data": mockAxiosParam});

        const wrapper = getWrapper();
        wrapper.setData({
            selectedReport: "minimal",
            selectedCommitId: "abcdef",
            error: "test-error",
            defaultMessage: "test-msg",
            parameterValues: []
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(mockAxios.history.get[0].url).toBe(url);
            expect(wrapper.find("#parameters").exists()).toBe(true);
            expect(wrapper.vm.$data.parameterValues).toMatchObject(mockAxiosParam);
            expect(wrapper.vm.$data.error).toBe("");
            expect(wrapper.vm.$data.defaultMessage).toBe("");
            done();
        });

    });

    it("parameters endpoint can set defaultmessage when errored", (done) => {
        const url = "http://app/report/minimal/config/parameters/?commit=test-commit"
        mockAxios.onGet(url)
            .reply(500, "Parameter fetching error");

        const wrapper = getWrapper();
        wrapper.setData({
            selectedReport: "minimal",
            selectedCommitId: "test-commit",
            error: "",
            defaultMessage: ""
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(mockAxios.history.get[0].url).toBe(url);
            expect(wrapper.find("#parameters").exists()).toBe(false);
            expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred when getting parameters");
            done();
        });
    });

    it("does not render parameters control if parameters and selected report data do not exist", () => {
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                initialGitBranches
            },
            data() {
                return {
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
        await Vue.nextTick();
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
        const param_url = "http://app/report/test-report/config/parameters/?commit=test-commit"
        mockAxios.onGet(param_url)
            .reply(200, {"data": []});

        const url = 'http://app/report/test-report/actions/run/';
        mockAxios.onPost(url)
            .reply(200, {data: {key: "test-key"}});

        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    instances_supported: true,
                    instances: {
                        annexe: ["a1", "a2"],
                        source: ["uat", "science", "prod"]
                    }
                },
                initialGitBranches
            },
            data() {
                return {
                    selectedReport: "report",
                    parameterValues: [{name: "minimal", value: "oldValue"}, {name: "global", value: "oldValue"}]
                }
            }
        });

        expect(wrapper.find(Instances).emitted().selectedValues.length).toBe(1)
        expect(wrapper.find(Instances).emitted().selectedValues[0][0]).toEqual({"annexe": "a1", "source": "uat"})
        setTimeout(async () => { //give the wrapper time to fetch reports
            wrapper.setData({
                selectedReport: "test-report",
                selectedCommitId: "test-commit",
                error: "test-error",
                defaultMessage: "test-msg"
            });
            await Vue.nextTick()
            wrapper.find(Instances).setData({selectedInstances: {source: "science", annexe: "a1"}})
            expect(wrapper.find(Instances).emitted().selectedValues.length).toBe(1)
            expect(wrapper.find(Instances).emitted().selectedValues[0][0]).toEqual({"annexe": "a1", "source": "science"})
            wrapper.setData({
                parameterValues: [{name: "minimal", value: "test"}, {name: "global", value: "random_39id"}],
            })
            await Vue.nextTick()
            wrapper.find("#run-form-group button").trigger("click");
            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(2);
                expect(mockAxios.history.get[1].url).toBe(param_url);
                expect(mockAxios.history.post[0].url).toBe(url);
                expect(mockAxios.history.post[0].data).toBe(JSON.stringify(
                    {
                        "instances": {
                            "source": "science",
                            "annexe": "science"
                        },
                        "params": {
                            "minimal": "test",
                            "global": "random_39id"

                        },
                        changelog: null,
                        "gitBranch": "master",
                        "gitCommit": "test-commit"
                    }
                ));
                expect(wrapper.find("#run-report-status").text()).toContain("Run started");
                expect(wrapper.find("#run-report-status a").text()).toBe("View log");
                expect(wrapper.find("#run-form-group button").attributes("disabled")).toBe("disabled");
                expect(wrapper.vm.$data.runningKey).toBe("test-key");
                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");
                done();
            });
        });
    });

    it("clicking run button sends changelog with request if set", async (done) => {
        const param_url = "http://app/report/test-report/parameters/?commit=test-commit"
        mockAxios.onGet(param_url)
            .reply(200, {"data": []});

        const url = 'http://app/report/test-report/actions/run/';
        mockAxios.onPost(url)
            .reply(200, {data: {key: "test-key"}});

        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: false,
                    instances_supported: false,
                    changelog_types: ["internal", "public"]
                },
                initialGitBranches: []
            },
            data() {
                return {
                    selectedReport: "report",
                    parameterValues: []
                }
            }
        });
        setTimeout(async () => { //give the wrapper time to fetch reports
            wrapper.setData({
                selectedReport: "test-report"
            });
            await Vue.nextTick();

            wrapper.setData({
                changeLogMessageValue: "test changelog"
            });

            wrapper.find("#run-form-group button").trigger("click");
            setTimeout(() => {
                expect(mockAxios.history.post[0].url).toBe(url);
                expect(mockAxios.history.post[0].data).toBe(JSON.stringify(
                    {
                        instances: {},
                        params: {},
                        changelog: {message: "test changelog", type: "internal"},
                        gitBranch: "",
                        gitCommit: ""
                    }
                ));
                expect(wrapper.find("#run-report-status").text()).toContain("Run started");
                expect(wrapper.vm.$data.runningKey).toBe("test-key");
                expect(wrapper.emitted("update:key").length).toBe(1);
                expect(wrapper.emitted("update:key")[0][0]).toBe("test-key");

                done();
            });
        });
    });

    it("clicking run button sends run request and sets error", async (done) => {
        const url = 'http://app/report/test-report/actions/run/';
        mockAxios.onPost(url)
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
                expect(wrapper.emitted("update:key")).toBeUndefined();
                done();
            });
        });
    });

    it("clicking 'View log' emits 'changeTab'", async (done) => {
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

            wrapper.find("#run-form-group a").trigger("click");

            setTimeout(() => {
                expect(wrapper.emitted("changeTab").length).toBe(1);
                done();
            });
        });
    });

    it("changing selectedReport resets runningStatus and enables run", async () => {
        const wrapper = getWrapper();
        await Vue.nextTick();
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
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    instances_supported: true,
                    instances: {
                        source: ["prod", "uat"],
                    }
                },
                initialGitBranches
            }
        });
        await Vue.nextTick();
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

    it("it does render changelog message and type correctly if a report is selected", () => {
        const changelogTypes =  ["internal", "public"]
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    changelog_types: changelogTypes
                },
                initialGitBranches
            },
            data() {
                return {
                    selectedReport: "report"
                }
            }
        });

        expect(wrapper.find("#changelog-message").exists()).toBe(true);
        const options = wrapper.find("#changelog-type").find("select")
            .findAll("option")
        expect(options.at(0).text()).toBe(changelogTypes[0]);
        expect(options.at(1).text()).toBe(changelogTypes[1]);
    });

    it("it does not render changelog message and type if a report is not selected", () => {
        const changelogTypes =  ["internal", "public"]
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: false,
                    changelog_types: changelogTypes,
                },
                initialGitBranches
            }
        });
        expect(wrapper.find("#changelog-message").exists()).toBe(false);
        expect(wrapper.find("#changelog-type").exists()).toBe(false);
    });

    it("it can accepts changelog  and type log message values", () => {
        const changelogTypes =  ["internal", "public"]
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    changelog_types: changelogTypes,
                },
                initialGitBranches
            },
            data() {
            return {
                selectedReport: "report",
                changeLogMessageValue: "Text area message",
                changeLogTypeValue: "selectedType"
            }
        }
        });

        const label = ["col-form-label", "col-sm-2", "text-right"]
        const control = ["col-sm-6"]

        const options = wrapper.find("#changelogType")
            .find("select").findAll("option")
        options.at(1).setSelected()
        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public")

        wrapper.find("#changelogMessage").setValue("New message")
        expect(wrapper.vm.$data.changeLogMessageValue).toBe("New message")

        const changelogMessage = wrapper.find(changeLog).find("#changelog-message")
        const changelogType= wrapper.find(changeLog).find("#changelog-type")

        expect(changelogMessage.find("label").classes()).toEqual(label)
        expect(changelogMessage.find("#change-message-control").classes()).toEqual(control)
        expect(changelogType.find("label").classes()).toEqual(label)
        expect(changelogType.find("#change-type-control").classes()).toEqual(control)
    });

    it("it does not disable runButton or display error msg when parameters pass validation", async () => {
        const localParam = [
            {name: "global", value: "Set new value"},
            {name: "max", value: "James bond"},
        ]
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true
                },
                initialGitBranches
            },
            data() {
                return {
                    parameterValues: localParam,
                    selectedReport: "reports"
                }
            }
        });
        expect(wrapper.find("#parameters").exists()).toBe(true);
        expect(wrapper.vm.$data.parameterValues.length).toBeGreaterThan(0)
        expect(wrapper.vm.$data.disableRun).toBe(false)
    });

    it("can run validation when component is loaded and disables runButton when parameters fail validation", async () => {
        const localParam = [
            {name: "global", value: "Set new value"},
            {name: "minimal", value: ""}
        ]
        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                },
                initialGitBranches
            },
            data() {
                return {
                    selectedReport: "reports",
                    parameterValues: localParam
                }
            }
        });
        expect(wrapper.find("#parameters").exists()).toBe(true);
        expect(wrapper.vm.$data.parameterValues.length).toBeGreaterThan(0)
        expect(wrapper.vm.$data.disableRun).toBe(true)
    });
});
