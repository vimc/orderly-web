import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import Vue from "vue";
import runWorkflowReport from "../../../js/components/runWorkflow/runWorkflowReport.vue";
import GitUpdateReports from "../../../js/components/runReport/gitUpdateReports.vue";
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

    it("does not render content until workflowMetadata and run report metadta are both set", (done) => {
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
        const wrapper = getWrapper()
        setTimeout(() => {
            expect(wrapper.find("#add-report-header").text()).toBe("Add reports")
            expect(wrapper.find("#git-header").text()).toBe("Git")
            expect(wrapper.find("#report-sub-header").text()).toBe("Reports")
            done();
        });
    });

    it(`it renders workflow preprocess menu correctly`, (done) => {
        const wrapper = getWrapper()
        setTimeout(() => {
            const preprocessor = wrapper.find("#preprocess-div")
            expect(preprocessor.findAll("label").at(0).text()).toBe("Preprocess")
            expect(preprocessor.findAll("label").at(1).text()).toBe("nmin:")
            expect(preprocessor.findAll("label").at(2).text()).toBe("nmax:")

            expect(preprocessor.find("input#n-min").exists()).toBe(true)
            expect(wrapper.find("#workflow-remove-button").text()).toBe("Remove report")
            expect(preprocessor.find("input#n-max").exists()).toBe(true)
            done();
        });
    })

    it(`it renders Add report menu correctly`, (done) => {
        const wrapper = getWrapper()
        setTimeout(() => {
            const report = wrapper.find("#add-report-div")
            expect(report.find("label").text()).toBe("Add report")
            expect(report.find("input#workflow-report").exists()).toBe(true)
            expect(report.find("#add-report-button").text()).toBe("Add report")
            done();
        })
    })

    it(`it can set and render props correctly`, async() => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
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
});
