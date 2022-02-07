import {mockAxios} from "../../../mockAxios";
import {shallowMount, mount} from "@vue/test-utils";
import Vue from "vue";
import runWorkflowReport from "../../../../js/components/runWorkflow/runWorkflowReport.vue";
import GitUpdateReports from "../../../../js/components/runReport/gitUpdateReports.vue";
import ReportList from "../../../../js/components/runReport/reportList.vue";
import ParameterList from "../../../../js/components/runReport/parameterList.vue";
import ErrorInfo from "../../../../js/components/errorInfo.vue";
import {BAlert} from "bootstrap-vue";
import {switches} from "../../../../js/featureSwitches";
import {session} from "../../../../js/utils/session";
import {mockRunWorkflowMetadata, mockRunReportMetadata} from "../../../mocks";

export const runReportMetadataResponse = {
    metadata: {
        instances_supported: false,
        git_supported: true,
        instances: {"source": []},
        changelog_types: ["published", "internal"]
    },
    git_branches: ["master", "dev"]
};

const minimal = { name: "minimal", date: null };
const global = { name: "other", date: new Date() };

const workflowValidationResponse = {
    data: [
        {name: "minimal", params: {nmin: "5"}},
        {name: "global", params: {p1: "v1", p2: "v2"}}
    ]
};

const mockSessionSetSelectedWorkflowReportSource = jest.fn();
const mockSessionGetSelectedWorkflowReportSource = jest.fn();

describe(`runWorkflowReport`, () => {
    beforeEach(() => {
        mockAxios.reset();

        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": mockRunReportMetadata()});

        const url = "http://app/workflow/validate/?branch=branch&commit=abc123"
        mockAxios.onPost(url).replyOnce(200, workflowValidationResponse);

        mockAxios.onPost("http://app/workflow/validate/?branch=test&commit=test")
            .replyOnce(500, {errors: [
                {code: "bad-request", message: "ERROR 1"},
                {code: "bad-request", message: "ERROR 2"}
            ]});

        session.setSelectedWorkflowReportSource = mockSessionSetSelectedWorkflowReportSource;
        session.getSelectedWorkflowReportSource = mockSessionGetSelectedWorkflowReportSource;

        jest.resetAllMocks();
    });

    const getWrapper = (propsData = {workflowMetadata: mockRunWorkflowMetadata()}) => {
        return shallowMount(runWorkflowReport, {propsData})
    };

    it("fetches report run metadata and renders gitUpdateReports component", (done) => {
        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": mockRunReportMetadata()});

        const wrapper = getWrapper({
            workflowMetadata: mockRunWorkflowMetadata({git_branch: "master", git_commit: "abc123"})
        });
        setTimeout(() => {
            const git = wrapper.findComponent(GitUpdateReports);
            expect(git.props("reportMetadata")).toStrictEqual(mockRunReportMetadata().metadata);
            expect(git.props("initialBranches")).toStrictEqual(mockRunReportMetadata().git_branches);
            expect(git.props("initialBranch")).toBe("master");
            expect(git.props("initialCommitId")).toBe("abc123");
            expect(git.props("showAllReports")).toBe(true);

            const error = wrapper.findComponent(ErrorInfo);
            expect(error.props("apiError")).toBe("");
            expect(error.props("defaultMessage")).toBe("");

            done();
        });
    });

    it("renders error when error on fetch report run metadata", (done) => {
        const testError = {test: "something"};
        mockAxios.onGet('http://app/report/run-metadata')
            .reply(500, testError);

        const wrapper = getWrapper();
        setTimeout(() => {
            const git = wrapper.findComponent(GitUpdateReports);
            expect(git.exists()).toBe(false);

            const error = wrapper.findComponent(ErrorInfo);
            expect(error.props("apiError").response.data).toStrictEqual(testError);
            expect(error.props("defaultMessage")).toBe("An error occurred fetching run report metadata");
            done();
        });
    });

    it("does not render content until run report metadata is set", (done) => {
        const wrapper = getWrapper();
        expect(wrapper.findComponent(GitUpdateReports).exists()).toBe(false);
        setTimeout(async () => {
            expect(wrapper.findComponent(GitUpdateReports).exists()).toBe(true);
            done();
        });
    });

    it(`it renders workflow report headers correctly`, (done) => {
        const wrapper = getWrapper();
        setTimeout(() => {
            expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
            expect(wrapper.find("#git-header").text()).toBe("Git")
            expect(wrapper.find("#report-sub-header").text()).toBe("Reports")
            done();
        });
    });

    it("emits update and clears validation errors on branch selected ", (done) => {
        const wrapper = getWrapper();
        wrapper.setData({
            validationErrors: [{message: "TEST ERROR", code: "error"}]
        });
        setTimeout(async () => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("branchSelected", "dev");
            await Vue.nextTick();
            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({git_branch: "dev"});
            expect(wrapper.vm.$data.validationErrors).toStrictEqual([]);
            done();
        });
    });

    it("emits update and clears validation errors on commit selected", (done) => {
        const wrapper = getWrapper();
        wrapper.setData({
            validationErrors: [{message: "TEST ERROR", code: "error"}]
        });
        setTimeout(async () => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("commitSelected", "xyz987");
            await Vue.nextTick();
            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({git_commit: "xyz987"});
            expect(wrapper.vm.$data.validationErrors).toStrictEqual([]);
            done();
        });
    });

    it("updates reports from git component", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            const reports = [minimal, global];
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", reports);
            await Vue.nextTick();
            expect(wrapper.vm.$data.reports).toBe(reports);
            done();
        });
    });

    it("clears selected report on update reports if it is no longer in the list", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            wrapper.setData({selectedReport: {name: "global"}});
            const reports = [minimal, global];
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", reports);
            await Vue.nextTick();
            expect(wrapper.vm.$data.selectedReport).toBeNull();
            done();
        });
    });

    it("does not clear selected report on update reports if it is in the new list", async () => {
        const wrapper = getWrapper();
        await wrapper.setData({selectedReport: {name: "other"}});
        await Vue.nextTick();
        await Vue.nextTick();
        const reports = [minimal, global];
        wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", reports);
        expect(wrapper.vm.$data.selectedReport.name).toBe("other");
    });


    it("renders add report as expected", (done) => {
        const wrapper = getWrapper();
        const reports = [minimal, global];
        wrapper.setData({reports, selectedReport: {name: "other"}});
        setTimeout(() => {
            const addReportContainer = wrapper.find("#add-report-div");
            expect(addReportContainer.exists()).toBe(true);
            expect(addReportContainer.find("label").text()).toBe("Add report");
            const reportList = wrapper.findComponent(ReportList);
            expect(reportList.props("reports")).toBe(reports);
            expect(reportList.props("selectedReport")).toStrictEqual({name: "other"});
            const button = addReportContainer.find("#add-report-button");
            expect(button.attributes("disabled")).toBeUndefined();
            expect(button.text()).toBe("Add report");
            done();
        });
    });

    it("add report button is disabled if no selected report", (done) => {
        const wrapper = getWrapper();
        wrapper.setData({reports: [minimal, global]});
        setTimeout(() => {
            const button = wrapper.find("#add-report-button");
            expect(button.attributes("disabled")).toBe("disabled");
            done();
        });
    });

    it("renders workflow reports as expected", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    reports: [
                        {"name": "minimal"},
                        {"name": "other", "params": {p1: "v1", p2: "v2"}}
                    ]
                })
        });
        setTimeout(() => {
            const workflowReports = wrapper.find("#workflow-reports");

            const report1Div = workflowReports.find("#workflow-report-0");
            expect(report1Div.find("label").text()).toBe("minimal");
            expect(report1Div.find("label").attributes("title")).toBe("minimal");
            expect(report1Div.find("parameter-list-stub").exists()).toBe(false);
            expect(report1Div.find(".no-parameters").text()).toBe("No parameters");
            expect(report1Div.find(".remove-report-button").text()).toBe("Remove report");

            const report2Div = workflowReports.find("#workflow-report-1");
            expect(report2Div.find("label").text()).toBe("other");
            expect(report2Div.find("label").attributes("title")).toBe("other");
            expect(report2Div.find("parameter-list-stub").exists()).toBe(true);
            expect(report2Div.find("parameter-list-stub").props("params")).toStrictEqual([
                {"name": "p1", "value": "v1"},
                {"name": "p2", "value": "v2"}
            ]);
            expect(report2Div.find(".no-parameters").exists()).toBe(false);
            expect(report2Div.find(".remove-report-button").text()).toBe("Remove report");

            expect(wrapper.findComponent(BAlert).props("show")).toBe(false);

            done();
        });
    });

    it("Clicking add report button fetches parameters and emits expected workflow metadata update", (done) => {
        mockAxios.onGet('http://app/report/other/config/parameters/?commit=abc123')
            .reply(200, {data: [{name: "p1", value: "v1"}, {name: "p2", value: "v2"}]});

        const wrapper = getWrapper({
            workflowMetadata: mockRunWorkflowMetadata({
                git_commit: "abc123",
                reports: [minimal]
            }),
        });
        wrapper.setData({
            reports: [minimal, global],
            selectedReport: {name: "other"},
            error: "previous error",
            defaultMessage: "previous Message"
        });
        setTimeout(() => {
            wrapper.find("#add-report-button").trigger("click");
            setTimeout(() => {
                expect(wrapper.emitted("update").length).toBe(1);
                expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                    reports: [
                        minimal,
                        {name: "other", params: {p1: "v1", p2: "v2"}}
                    ]
                });
                expect(wrapper.vm.$data.selectedReport).toBe(null);
                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");
                done();
            });
        });
    });

    it("Error from adding report is rendered", (done) => {
        const testError = {test: "something"};
        mockAxios.onGet('http://app/report/other/config/parameters/')
            .reply(500, testError);

        const wrapper = getWrapper();
        wrapper.setData({
            reports: [minimal, global],
            selectedReport: {name: "other"}
        });
        setTimeout(() => {
            wrapper.find("#add-report-button").trigger("click");
            setTimeout(() => {
                expect(wrapper.emitted("update")).toBeUndefined();
                expect(wrapper.vm.$data.selectedReport).toStrictEqual({name: "other"});
                const error = wrapper.findComponent(ErrorInfo);
                expect(error.props("apiError").response.data).toStrictEqual(testError);
                expect(error.props("defaultMessage")).toBe("An error occurred when getting parameters");
                done();
            });
        });
    });

    it("Clicking remove report emits expected workflow metadata update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [{name: "minimal"}, {name: "other", params: {p1: "v1"}}]
                }),
        });
        wrapper.setData({
            reports: [minimal, global],
            selectedReport: {name: "other"}
        });
        setTimeout(() => {
            wrapper.findAll(".remove-report-button").at(1).trigger("click");

            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                reports: [{name: "minimal"}]
            });
            done();

        });
    });

    it("updating parameter values emits expected workflow metadata update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    reports: [
                        {name: "minimal", params: {nmin: "8"}},
                        {name: "other", params: {p1: "v1", p2: "v2"}}
                    ]
                }),
        });
        setTimeout(() => {
            wrapper.findAllComponents(ParameterList).at(0).vm.$emit("paramsChanged", [{name: "nmin", value: "10"}]);
            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                reports: [
                    {name: "minimal", params: {nmin: "10"}},
                    {name: "other", params: {p1: "v1", p2: "v2"}}
                ]
            });
            done();
        });
    });

    it("can remove obsolete reports from workflow on available reports update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [
                        {name: "minimal"},
                        {name: "nonexistent"},
                        {name: "global"}
                    ]
                }),
        });
        const newAvailableReports = [
            {name: "minimal", date: null},
            {name: "global", date: new Date()},
            {name: "another", date: null}
        ];
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123').reply(200, {data: []});
        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abc123').reply(200, {data: []});
        mockAxios.onGet('http://app/report/another/config/parameters/?commit=abc123').reply(200, {data: []});
        setTimeout(() => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", newAvailableReports);

            setTimeout(() => {
                expect(wrapper.emitted("update").length).toBe(1);
                expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                    reports: [
                        {name: "minimal", params: {}},
                        {name: "global", params: {}}
                    ]
                });

                const alert = wrapper.findComponent(BAlert);
                expect(alert.props("show")).toBe(true);
                expect(alert.text()).toContain("The following items are not present in this git commit and have been removed from the workflow:");
                expect(alert.findAll("li").length).toBe(1);
                expect(alert.findAll("li").at(0).text()).toBe("Report 'nonexistent'");
                done();
            });
        });
    });

    it("can remove obsolete parameters from workflow reports on available reports update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [
                        {name: "minimal", params: {nmin: "5"}},
                        {name: "global", params: {p1: "v1", p2: "v2", p3: "v3"}}
                    ]
                })
        });
        const newAvailableReports = [
            {name: "minimal", date: null},
            {name: "global", date: new Date()}
        ];
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123').reply(200, {data: []});
        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abc123').reply(200, {data: [
            {name: "p2", value: "newValue"}
        ]});

        setTimeout(() => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", newAvailableReports);

            setTimeout(() => {
                expect(wrapper.emitted("update").length).toBe(1);
                expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                    reports: [
                        {name: "minimal", params: {}},
                        {name: "global", params: {p2: "v2"}}
                    ]
                });

                const alert = wrapper.findComponent(BAlert);
                expect(alert.props("show")).toBe(true);
                expect(alert.findAll("li").length).toBe(3);
                expect(alert.findAll("li").at(0).text()).toBe("Parameter 'nmin' in report 'minimal'");
                expect(alert.findAll("li").at(1).text()).toBe("Parameter 'p1' in report 'global'");
                expect(alert.findAll("li").at(2).text()).toBe("Parameter 'p3' in report 'global'");
                done();
            });
        });
    });

    it("can add new parameters to workflow reports on available reports update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [
                        {name: "minimal", params: {nmin: "5"}},
                        {name: "global"}
                    ]
                })
        });
        const newAvailableReports = [
            {name: "minimal", date: null},
            {name: "global", date: new Date()}
        ];
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123').reply(200, {data: [
            {name: "aNewParam", value: "1"},
            {name: "nmin", value: "6"}
        ]});
        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abc123').reply(200, {data: [
            {name: "p1", value: "v1"},
            {name: "p2", value: "v2"}
         ]});

        setTimeout(() => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", newAvailableReports);

            setTimeout(() => {
                expect(wrapper.emitted("update").length).toBe(1);
                expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                    reports: [
                        {name: "minimal", params: {aNewParam: "1", nmin: "5"}},
                        {name: "global", params: {p1: "v1", p2: "v2"}}
                    ]
                });

                expect(wrapper.findComponent(BAlert).props("show")).toBe(false);
                done();
            });
        });
    });

    it("can combine workflow changes on available reports update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [
                        {name: "minimal", params: {nmin: "5"}},
                        {name: "global", params: {p1: "v1", p2: "v2"}}
                    ]
                })
        });
        const newAvailableReports = [
            {name: "global", date: new Date()}
        ];
        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abc123').reply(200, {data: [
                {name: "p2", value: "newValue2"},
                {name: "p3", value: "newValue3"}
            ]});

        setTimeout(() => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", newAvailableReports);

            setTimeout(() => {
                expect(wrapper.emitted("update").length).toBe(1);
                expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                    reports: [
                        {name: "global", params: {p2: "v2", p3: "newValue3"}}
                    ]
                });

                const alert = wrapper.findComponent(BAlert);
                expect(alert.props("show")).toBe(true);
                expect(alert.findAll("li").length).toBe(2);
                expect(alert.findAll("li").at(0).text()).toBe("Report 'minimal'");
                expect(alert.findAll("li").at(1).text()).toBe("Parameter 'p1' in report 'global'");
                done();
            });
        });
    });

    it("renders error received when checking parameters on update available reports", (done) => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [
                        {name: "minimal", params: {nmin: "5"}}
                    ]
                })
        });
        const newAvailableReports = [
            {name: "minimal", date: new Date()}
        ];

        setTimeout(() => {
            const testError = {test: "something"};
            mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123').reply(500, testError);
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", newAvailableReports);

            setTimeout(() => {
                const error = wrapper.findComponent(ErrorInfo);
                expect(error.props("apiError").response.data).toStrictEqual(testError);
                expect(error.props("defaultMessage")).toBe("An error occurred when refreshing parameters");
                done();
            });
        });
    });

    it("can dismiss workflow removals alert", async () => {
        const wrapper = mount(runWorkflowReport, {propsData: {workflowMetadata: mockRunWorkflowMetadata()}});
        wrapper.setData({workflowRemovals: ["Test removal"], runReportMetadata: {}});
        await Vue.nextTick();

        const dismissButton = wrapper.findComponent(BAlert).find("button.close");
        dismissButton.trigger("click");
        await Vue.nextTick();
        expect(wrapper.vm.$data.workflowRemovals).toStrictEqual(null);
        expect(wrapper.findComponent(BAlert).props("show")).toBe(false);
    });

    it("can validate imported workflow reports", (done) => {
        switches.workFlowReport = true
        const url = "http://app/workflow/validate/?branch=branch&commit=abc123"

        const blob = new Blob(["report"], {type: 'text/csv'});
        blob['name'] = "test.csv";
        const fakeFile = <File>blob;

        const mockUpdateWorkflowReports = jest.fn()

        const wrapper = shallowMount(runWorkflowReport, {
            propsData: {
                workflowMetadata:
                    mockRunWorkflowMetadata({
                        git_commit: "abc123",
                        git_branch: "branch"
                    }),
            },
            methods: {
                updateWorkflowReports: mockUpdateWorkflowReports
            }
        });

        wrapper.setData({validationErrors: [{message: "old error", code: "TEST"}]});
        setTimeout(async () => {
            await wrapper.find("input#import-from-csv").trigger("click")

            expect(wrapper.vm.$data.reportsOrigin).toBe("csv")

            expect(wrapper.find("#show-import-csv").exists()).toBe(true)

            const input = wrapper.find("input#import-csv.custom-file-input").element as HTMLInputElement

            Object.defineProperty(input, "files", {
                value: [fakeFile]
            })

            wrapper.find("input#import-csv.custom-file-input").trigger("change")

            setTimeout(() => {
                expect(wrapper.vm.$data.importedFilename).toBe("test.csv")
                expect(wrapper.vm.$data.importedFile).toMatchObject({})
                expect(mockAxios.history.post.length).toBe(1)
                expect(mockAxios.history.post[0].url).toBe(url)
                expect(mockAxios.history.post[0].data).toMatchObject({})

                expect(wrapper.vm.$data.validationErrors).toStrictEqual([]);
                const errorAlert = wrapper.findAllComponents(BAlert).at(1);
                expect(errorAlert.attributes("id")).toBe("import-validation-errors");
                expect(errorAlert.props("show")).toBe(false);

                expect(mockUpdateWorkflowReports.mock.calls.length).toBe(1)
                expect(mockUpdateWorkflowReports.mock.calls[0][0]).toEqual(
                    [
                        {name: "minimal", params: {nmin: "5"}},
                        {name: "global", params: {p1: "v1", p2: "v2"}}
                    ])

                expect(wrapper.emitted("valid").length).toBe(1);
                expect(wrapper.emitted("valid")[0][0]).toBe(true);

                done()
            })
        });
    });


    it("remove imported file when a report is manually removed from imported reports", async () => {
        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [{name: "minimal"}, {name: "other", params: {p1: "v1"}}]
                })
        });

        await Vue.nextTick()

        await wrapper.setData({
            reports: [minimal, global],
            selectedReport: {name: "other"},
            isImportedReports: true,
            importedFilename: "test Filename",
            importedFile: {test: "test blob"}
        });

        await Vue.nextTick()

        await wrapper.findAll(".remove-report-button").at(1).trigger("click");
        expect(wrapper.vm.$data.importedFilename).toBe("")
        expect(wrapper.vm.$data.importedFile).toBe(null)
        expect(wrapper.vm.$data.isImportedReports).toBe(false)
    });

    it("remove imported file when a report is manually added to imported reports", async () => {
        mockAxios.onGet('http://app/report/other/config/parameters/?commit=abc123')
            .reply(200, {data: [{name: "p1", value: "v1"}, {name: "p2", value: "v2"}]});

        const wrapper = getWrapper({
            workflowMetadata:
                mockRunWorkflowMetadata({
                    git_commit: "abc123",
                    reports: [minimal]
                })
        });

        await Vue.nextTick()

        await wrapper.setData({
            reports: [minimal, global],
            selectedReport: {name: "other"},
            isImportedReports: true,
            importedFilename: "test Filename",
            importedFile: {test: "test blob"}
        });

        await Vue.nextTick()

        await wrapper.find("#add-report-button").trigger("click");
        await Vue.nextTick();
        expect(wrapper.vm.$data.importedFilename).toBe("")
        expect(wrapper.vm.$data.importedFile).toBe(null)
        expect(wrapper.vm.$data.isImportedReports).toBe(false)
    });

    it("can display errors if workflow validation fails",  (done) => {
        switches.workFlowReport = true
        const url = "http://app/workflow/validate/?branch=test&commit=test"

        const blob = new Blob(["invalid content"], {type: 'text/csv'});
        blob['name'] = "test.csv";
        const fakeFile = <File>blob

        const mockUpdateWorkflowReports = jest.fn()

        const wrapper = shallowMount(runWorkflowReport, {
            propsData: {
                workflowMetadata:
                    mockRunWorkflowMetadata({
                        git_commit: "test",
                        git_branch: "test",
                        reports: [
                            {name: "test report"} as any
                        ]
                    })
            },
            methods: {
                updateWorkflowReports: mockUpdateWorkflowReports
            }
        });
        wrapper.setData({
            reportsValid: [true]
        });

        setTimeout(async () => {
            await wrapper.find("input#import-from-csv").trigger("click")

            expect(wrapper.vm.$data.reportsOrigin).toBe("csv")

            expect(wrapper.find("#show-import-csv").exists()).toBe(true)

            const input = wrapper.find("input#import-csv.custom-file-input").element as HTMLInputElement

            Object.defineProperty(input, "files", {
                value: [fakeFile]
            })

            expect(wrapper.emitted("valid").length).toBe(1);
            expect(wrapper.emitted("valid")[0][0]).toBe(true);

            await wrapper.find("input#import-csv.custom-file-input").trigger("change")

            setTimeout(async () => {
                expect(wrapper.vm.$data.importedFilename).toBe("test.csv")
                expect(wrapper.vm.$data.importedFile).toMatchObject({})
                expect(mockAxios.history.post.length).toBe(1)
                expect(mockAxios.history.post[0].url).toBe(url)
                expect(mockAxios.history.post[0].data).toMatchObject({})
                expect(wrapper.vm.$data.validationErrors).toEqual([
                    {code: "bad-request", message: "ERROR 1"},
                    {code: "bad-request", message: "ERROR 2"}
                ]);
                expect(mockUpdateWorkflowReports.mock.calls.length).toBe(1);
                expect(mockUpdateWorkflowReports.mock.calls[0][0]).toStrictEqual([]);

                expect(wrapper.emitted("valid").length).toBe(2);
                expect(wrapper.emitted("valid")[1][0]).toBe(false);


                const errorAlert = wrapper.findAllComponents(BAlert).at(1);
                expect(errorAlert.attributes("id")).toBe("import-validation-errors");
                expect(errorAlert.props("show")).toBe(true);
                expect(errorAlert.text()).toContain("Failed to import from csv. The following issues were found:");
                const errors = wrapper.findAll("li.import-validation-error");
                expect(errors.length).toBe(2);
                expect(errors.at(0).text()).toBe("ERROR 1");
                expect(errors.at(1).text()).toBe("ERROR 2");

                //Test dismissing BAlert clears validation errors
                errorAlert.vm.$emit("dismissed");
                expect(wrapper.vm.$data.validationErrors).toStrictEqual([]);

                await Vue.nextTick();
                expect(errorAlert.props("show")).toBe(false);

                done();
            })
        });
    });

    it("clears file input value when clicked", (done) => {
        const wrapper = shallowMount(runWorkflowReport, {
            propsData: {
                workflowMetadata: mockRunWorkflowMetadata({
                    git_commit: "test",
                    git_branch: "test"
                })
            }
        });

        setTimeout(async () => {
            await wrapper.find("#import-from-csv").trigger("click")

            const input = wrapper.find("input#import-csv.custom-file-input");
            const inputEl = input.element as HTMLInputElement;

            const spy = jest.spyOn(inputEl, 'value', 'set');

            await input.trigger("click");
            expect(spy).toHaveBeenCalledWith(null);
            done();
        });
    });

    it("renders choose or import from and check default radio button", (done) => {
        switches.workFlowReport = true
        const wrapper = getWrapper();

        setTimeout(() => {
            const fromListLabel = wrapper.find("#choose-from-list-label")
            expect(fromListLabel.text()).toBe("Choose from list")
            expect((fromListLabel.find("input").element as HTMLInputElement).checked).toBe(true)
            expect(wrapper.vm.$data.reportsOrigin).toBe("list")

            const fromCsvLabel = wrapper.find("#import-from-csv-label")
            expect(fromCsvLabel.text()).toBe("Import from csv")
            expect(fromCsvLabel.find("input").attributes("checked")).toBeUndefined()
            done();
        });
    });

   it("loads with import csv checked when set in session storage", (done) =>{
        const mockGetReportsSource = jest.fn();
        mockGetReportsSource.mockReturnValue("csv");
        session.getSelectedWorkflowReportSource = mockGetReportsSource;
        const wrapper = getWrapper();

        setTimeout(() => {
            expect(wrapper.vm.$data.reportsOrigin).toBe("csv");

            expect((wrapper.find("#choose-from-list").element as HTMLInputElement).checked).toBe(false);
            expect(wrapper.find("#choose-from-list-label").classes()).not.toContain("active");

            expect((wrapper.find("#import-from-csv").element as HTMLInputElement).checked).toBe(true);
            expect(wrapper.find("#import-from-csv-label").classes()).toContain("active");
            done();
        });
    });

    it("loads with choose from list checked when set in session storage", (done) =>{
        const mockGetReportsSource = jest.fn();
        mockGetReportsSource.mockReturnValue("list");
        session.getSelectedWorkflowReportSource = mockGetReportsSource;
        const wrapper = getWrapper();

        setTimeout(() => {
            expect(wrapper.vm.$data.reportsOrigin).toBe("list");

            expect((wrapper.find("#choose-from-list").element as HTMLInputElement).checked).toBe(true);
            expect(wrapper.find("#choose-from-list-label").classes()).toContain("active");

            expect((wrapper.find("#import-from-csv").element as HTMLInputElement).checked).toBe(false);
            expect(wrapper.find("#import-from-csv-label").classes()).not.toContain("active");
            done();
        });
    });

    it("does not show report from list component when import from csv is checked", (done) => {
        switches.workFlowReport = true
        const wrapper = getWrapper();

        setTimeout(async () => {
            const fromCsvLabel = wrapper.find("#import-from-csv-label")
            fromCsvLabel.find("input").trigger("click")
            expect(wrapper.vm.$data.reportsOrigin).toBe("csv")

            await Vue.nextTick()
            expect(wrapper.find("#show-report-list").exists()).toBe(false)
            expect(wrapper.find("#show-import-csv").exists()).toBe(true)
            done();
        });
    });

    it("shows report from list when choose from list is checked", (done) => {
        switches.workFlowReport = true
        const wrapper = getWrapper();

        setTimeout(() => {
            const fromListLabel = wrapper.find("#choose-from-list-label")
            fromListLabel.find("input").trigger("click")
            expect(wrapper.vm.$data.reportsOrigin).toBe("list")
            expect(wrapper.find("#show-import-csv").exists()).toBe(false)
            expect(wrapper.find("#show-report-list").exists()).toBe(true)
            done();
        });
    });

    it("renders import from csv controls as expected", (done) => {
        switches.workFlowReport = true
        const wrapper = getWrapper();

        setTimeout(async () => {
            const fromCsvLabel = wrapper.find("#import-from-csv-label")
            fromCsvLabel.find("input").trigger("click")
            expect(wrapper.vm.$data.reportsOrigin).toBe("csv")

            await Vue.nextTick()
            expect(wrapper.find("#show-import-csv").exists()).toBe(true)
            const uploadInput = wrapper.find("#show-import-csv").find("input")
            expect(uploadInput.attributes("accept")).toBe("text/csv")
            expect(uploadInput.attributes("lang")).toBe("en")

            // Should have updated session too
            expect(mockSessionSetSelectedWorkflowReportSource).toHaveBeenCalledWith("csv");

            done();
        });
    });

    it("can display filename", (done) => {
        switches.workFlowReport = true
        const wrapper = getWrapper();

        setTimeout(async () => {
            const fromCsvLabel = wrapper.find("#import-from-csv-label")
            fromCsvLabel.find("input").trigger("click")
            expect(wrapper.vm.$data.reportsOrigin).toBe("csv")

            await Vue.nextTick()
            expect(wrapper.find("#show-import-csv").exists()).toBe(true)
            const fakeFile = new File(["report"],  "test.csv", { type: 'text/csv'});
            const input = wrapper.find("#show-import-csv").find("input").element as HTMLInputElement

            expect(wrapper.vm.$data.importedFilename).toBe("")
            Object.defineProperty(input, "files", {
                value: [fakeFile]
            })

            wrapper.find("#show-import-csv").find("input").trigger("change")
            await Vue.nextTick()

            const uploadLabel = wrapper.find("#show-import-csv").find(".custom-file-label")
            expect(uploadLabel.text()).toBe("test.csv")
            expect(wrapper.vm.$data.importedFilename).toBe("test.csv")
            done();
        });
    });
});
