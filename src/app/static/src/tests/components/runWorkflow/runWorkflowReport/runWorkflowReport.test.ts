import {mockAxios} from "../../../mockAxios";
import {shallowMount, mount} from "@vue/test-utils";
import Vue from "vue";
import runWorkflowReport from "../../../../js/components/runWorkflow/runWorkflowReport.vue";
import GitUpdateReports from "../../../../js/components/runReport/gitUpdateReports.vue";
import ReportList from "../../../../js/components/runReport/reportList.vue";
import ParameterList from "../../../../js/components/runReport/parameterList.vue";
import ErrorInfo from "../../../../js/components/errorInfo.vue";
import {emptyWorkflowMetadata} from "../runWorkflowCreate.test";
import {BAlert} from "bootstrap-vue";

export const runReportMetadataResponse = {
    metadata: {
        instances_supported: false,
        git_supported: true,
        instances: {"source": []},
        changelog_types: ["published", "internal"]
    },
    git_branches: ["master", "dev"]
};

const reports = [
    { name: "minimal", date: null },
    { name: "other", date: new Date() }
];

describe(`runWorkflowReport`, () => {
    beforeEach(() => {
        mockAxios.reset();

        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": runReportMetadataResponse});
    });

    const getWrapper = (propsData = {workflowMetadata: {...emptyWorkflowMetadata}}) => {
        return shallowMount(runWorkflowReport, {propsData})
    };

    it("fetches report run metadata and renders gitUpdateReports component", (done) => {
        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": runReportMetadataResponse});

        const wrapper = getWrapper({
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_branch: "master",
                git_commit: "abc123"
            }
        });
        setTimeout(() => {
            const git = wrapper.findComponent(GitUpdateReports);
            expect(git.props("reportMetadata")).toStrictEqual(runReportMetadataResponse.metadata);
            expect(git.props("initialBranches")).toStrictEqual(runReportMetadataResponse.git_branches);
            expect(git.props("initialBranch")).toBe("master");
            expect(git.props("initialCommitId")).toBe("abc123");

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
        expect(wrapper.find(GitUpdateReports).exists()).toBe(false);
        setTimeout(async () => {
            expect(wrapper.find(GitUpdateReports).exists()).toBe(true);
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

    it("emits update on branch selected", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("branchSelected", "dev");
            await Vue.nextTick();
            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({git_branch: "dev"});
            done();
        });
    });

    it("emits update on commit selected", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            wrapper.findComponent(GitUpdateReports).vm.$emit("commitSelected", "xyz987");
            await Vue.nextTick();
            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({git_commit: "xyz987"});
            done();
        });
    });

    it("updates reports from git component", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            const reports = [
                { name: "minimal", date: null },
                { name: "other", date: new Date() }
            ];
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", reports);
            await Vue.nextTick();
            expect(wrapper.vm.$data.reports).toBe(reports);
            done();
        });
    });

    it("clears selected report on update reports if it is no longer in the list", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            wrapper.setData({selectedReport: "global"});
            const reports = [
                { name: "minimal", date: null },
                { name: "other", date: new Date() }
            ];
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", reports);
            await Vue.nextTick();
            expect(wrapper.vm.$data.selectedReport).toBe("");
            done();
        });
    });

    it("does not clear selected report on update reports if it is in the new list", (done) => {
        const wrapper = getWrapper();
        setTimeout(async () => {
            wrapper.setData({selectedReport: "other"});
            const reports = [
                { name: "minimal", date: null },
                { name: "other", date: new Date() }
            ];
            wrapper.findComponent(GitUpdateReports).vm.$emit("reportsUpdate", reports);
            await Vue.nextTick();
            expect(wrapper.vm.$data.selectedReport).toBe("other");
        });
        done();
    });


    it("renders add report as expected", (done) => {
        const wrapper = getWrapper();
        wrapper.setData({reports, selectedReport: "other"});
        setTimeout(() => {
            const addReportContainer = wrapper.find("#add-report-div");
            expect(addReportContainer.exists()).toBe(true);
            expect(addReportContainer.find("label").text()).toBe("Add report");
            const reportList = wrapper.findComponent(ReportList);
            expect(reportList.props("reports")).toBe(reports);
            expect(reportList.props("report")).toBe("other");
            const button = addReportContainer.find("#add-report-button");
            expect(button.attributes("disabled")).toBeUndefined();
            expect(button.text()).toBe("Add report");
            done();
        });
    });

    it("add report button is disabled if no selected report", (done) => {
        const wrapper = getWrapper();
        wrapper.setData({reports});
        setTimeout(() => {
            const button = wrapper.find("#add-report-button");
            expect(button.attributes("disabled")).toBe("disabled");
            done();
        });
    });

    it("renders workflow reports as expected", (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                reports: [
                    {"name": "minimal"},
                    {"name": "other", "params": {p1: "v1", p2: "v2"}}
                ]
            }
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
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [{name: "minimal"}]
            }
        });
        wrapper.setData({
            reports,
            selectedReport: "other",
            error: "previous error",
            defaultMessage: "previous Message"
        });
        setTimeout(() => {
            wrapper.find("#add-report-button").trigger("click");
            setTimeout(() => {
                expect(wrapper.emitted("update").length).toBe(1);
                expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                    reports: [
                        {name: "minimal"},
                        {name: "other", params: {p1: "v1", p2: "v2"}}
                    ]
                });
                expect(wrapper.vm.$data.selectedReport).toBe("");
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
            reports,
            selectedReport: "other"
        });
        setTimeout(() => {
            wrapper.find("#add-report-button").trigger("click");
            setTimeout(() => {
                expect(wrapper.emitted("update")).toBeUndefined();
                expect(wrapper.vm.$data.selectedReport).toBe("other");
                const error = wrapper.findComponent(ErrorInfo);
                expect(error.props("apiError").response.data).toStrictEqual(testError);
                expect(error.props("defaultMessage")).toBe("An error occurred when getting parameters");
                done();
            });
        });
    });

    it("Clicking remove report emits expected workflow metadata update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [{name: "minimal"}, {name: "other", params: {p1: "v1"}}]
            }
        });
        wrapper.setData({
            reports,
            selectedReport: "other"
        });
        setTimeout(() => {
            wrapper.findAll(".remove-report-button").at(1).trigger("click");

            expect(wrapper.emitted("update").length).toBe(1);
            expect(wrapper.emitted("update")[0][0]).toStrictEqual({
                reports: [
                    {name: "minimal"}
                ]
            });
            done();

        });
    });

    it("updating parameter values emits expected workflow metadata update", (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                reports: [
                    {name: "minimal", params: {nmin: "8"}},
                    {name: "other", params: {p1: "v1", p2: "v2"}}
                ]
            }
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
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [
                    {name: "minimal"},
                    {name: "nonexistent"},
                    {name: "global"}
                ]
            }
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
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [
                    {name: "minimal", params: {nmin: "5"}},
                    {name: "global", params: {p1: "v1", p2: "v2", p3: "v3"}}
                ]
            }
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
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [
                    {name: "minimal", params: {nmin: "5"}},
                    {name: "global"}
                ]
            }
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
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [
                    {name: "minimal", params: {nmin: "5"}},
                    {name: "global", params: {p1: "v1", p2: "v2"}}
                ]
            }
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
            workflowMetadata: {
                ...emptyWorkflowMetadata,
                git_commit: "abc123",
                reports: [
                    {name: "minimal", params: {nmin: "5"}}
                ]
            }
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
        const wrapper = mount(runWorkflowReport, {propsData: {workflowMetadata: {...emptyWorkflowMetadata}}});
        wrapper.setData({workflowRemovals: ["Test removal"], runReportMetadata: {}});
        await Vue.nextTick();

        const dismissButton = wrapper.findComponent(BAlert).find("button.close");
        dismissButton.trigger("click");
        await Vue.nextTick();
        expect(wrapper.vm.$data.workflowRemovals).toStrictEqual(null);
        expect(wrapper.findComponent(BAlert).props("show")).toBe(false);
    });

    it("renders choose or import from and check default radio button", (done) => {
        const wrapper = getWrapper();

        setTimeout(() => {
            const fromListLabel = wrapper.find("#choose-from-list-label")
            expect(fromListLabel.text()).toBe("Choose from list")
            expect(fromListLabel.find("input").attributes("checked")).toBe("checked")
            expect(wrapper.vm.$data.reportsOrigin).toBe("list")

            const fromCsvLabel = wrapper.find("#import-from-csv-label")
            expect(fromCsvLabel.text()).toBe("Import from csv")
            expect(fromCsvLabel.find("input").attributes("checked")).toBeUndefined()
            done();
        });
    });

    it("does not show report from list component when import from csv is checked", (done) => {
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
            done();
        });
    });

    it("can display filename", (done) => {
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
