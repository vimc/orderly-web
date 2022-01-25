import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummary.vue"
import runWorkflowSummaryHeader from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummaryHeader.vue"
import {RunWorkflowMetadata} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";
import workflowSummaryReports from "../../../../js/components/runWorkflow/workflowSummary/workflowSummaryReports.vue";

describe(`runWorkflowSummary`, () => {

    const workflowSummary = {
        ref: "refNum",
        missing_dependencies: {step2: ["step1"]},
        reports: [{name: "step2", params: {} }]
    }

    const workflowSummary2 = {
        refs: "test",
        missing_dependencies: {},
        reports: [{name: "test", params: {"key": "value"}}]
    }

    const metaData = {
        reports: [{name: "r1"}],
        git_commit: "gitCommit"
    }

    const metaData2 = {
        reports: [{name: "r1", params: {"key": "value"}}, {name: "r2"}],
        git_commit: "gitCommit"
    }

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onPost('http://app/workflows/summary')
            .reply(200, {"data": workflowSummary});
    })

    const getWrapper = (meta: Partial<RunWorkflowMetadata> = {reports: []}) => {
        return shallowMount(runWorkflowSummary, {propsData: {workflowMetadata: meta}})
    }

    it(`it posts to workflow summary endpoint and renders workflow summary header`, (done) => {
        const wrapper = getWrapper(metaData);
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe('http://app/workflows/summary');
            expect(wrapper.find(runWorkflowSummaryHeader).props("workflowSummary")).toStrictEqual(workflowSummary);
            done();
        });
    });

    it(`it can post workflow summary with dependencies as response`,  (done) => {
        mockAxios.onPost('http://app/workflows/summary')
            .reply(200, {"data": workflowSummary2});
            
        const wrapper = getWrapper(metaData2);

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1)
            expect(mockAxios.history.post[0].url).toBe('http://app/workflows/summary');
            const data = JSON.parse(mockAxios.history.post[0].data);
            expect(data.ref).toEqual("gitCommit")
            expect(data.reports).toEqual(metaData2.reports)
            expect(wrapper.findComponent(workflowSummaryReports).props("workflowSummary")).toEqual(workflowSummary2)
            done()
        })
    });

    it(`emits valid event on mount`, () => {
        const wrapper = getWrapper({
            reports: [{name: "r1"}]
        });
        expect(wrapper.emitted().valid[0][0]).toBe(true);
    });

    it(`error response from workflow summary endpoint generates error message`, async (done) => {
        mockAxios.onPost('http://app/workflows/summary')
            .reply(500, "TEST ERROR");

        const wrapper = getWrapper(metaData)
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            const errorMessage = wrapper.find("error-info-stub")
            expect(errorMessage.props("defaultMessage")).toBe("An error occurred while retrieving the workflow summary")
            expect(errorMessage.props("apiError")).toBeTruthy()
            done()
        });
    })

})