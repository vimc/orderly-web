import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import Vue from "vue";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue";
import GitUpdateReports from "../../../js/components/runReport/gitUpdateReports.vue";
import ReportList from "../../../js/components/runReport/reportList.vue";
import ParameterList from "../../../js/components/runReport/parameterList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {emptyWorkflowMetadata} from "./runWorkflowCreate.test";

export const runReportMetadataResponse = {
    metadata: {
        instances_supported: false,
        git_supported: true,
        instances: {"source": []},
        changelog_types: ["published", "internaal"]
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

    const getWrapper = (propsData = {workflowMetadata: {}}) => {
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
            expect(git.props("metadata")).toStrictEqual(runReportMetadataResponse.metadata);
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

    it("does not render content until workflowMetadata and run report metadata are both set", (done) => {
        const wrapper = getWrapper({workflowMetadata: null});
        expect(wrapper.find(GitUpdateReports).exists()).toBe(false);
        setTimeout(async () => {
            expect(wrapper.find(GitUpdateReports).exists()).toBe(false);
            wrapper.setProps({workflowMetadata: emptyWorkflowMetadata});
            await Vue.nextTick();
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

    it("Updates reports from git component", (done) => {
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
;        });
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
            expect(report1Div.find("parameter-list-stub").exists()).toBe(false);
            expect(report1Div.find(".no-parameters").text()).toBe("No parameters");
            expect(report1Div.find(".remove-report-button").text()).toBe("Remove report");

            const report2Div = workflowReports.find("#workflow-report-1");
            expect(report2Div.find("label").text()).toBe("other");
            expect(report2Div.find("parameter-list-stub").exists()).toBe(true);
            expect(report2Div.find("parameter-list-stub").props("params")).toStrictEqual([
                {"name": "p1", "value": "v1"},
                {"name": "p2", "value": "v2"}
            ]);
            expect(report2Div.find(".no-parameters").exists()).toBe(false);
            expect(report2Div.find(".remove-report-button").text()).toBe("Remove report");
            done();
        });
    });

    it("Clicking add report button fetches parameters and emits expected workflow metadata update", (done) => {
        mockAxios.onGet('http://app/report/other/parameters/?commit=abc123')
            .reply(200, {data: [{name: "p1", value: "v1"}, {name: "p2", value: "v2"}]});

        const wrapper = getWrapper({
            workflowMetadata: {
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
                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");
                done();
            });
        });
    });

    it("Error from adding report is rendered", (done) => {
        const testError = {test: "something"};
        mockAxios.onGet('http://app/report/other/parameters/')
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
});
