import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummary.vue"
import runWorkflowSummaryHeader from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummaryHeader.vue"
import {RunWorkflowMetadata} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";

describe(`runWorkflowSummary`, () => {

    const summaryData = {
        missing_dependencies: {
            step2: ["step1"]
        },
        ref: "refNum",
        reports: [
            {
                name: "step2",
                params: {}
            }
        ]
    }

    const metaData = {
        reports: [{name: "r1"}],
        git_commit: "gitCommit"
    }

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onPost('http://app/workflows/summary')
            .reply(200, {"data": summaryData});
    })

    const getWrapper = (meta: Partial<RunWorkflowMetadata> = {reports: []}) => {
        return shallowMount(runWorkflowSummary, {propsData: {workflowMetadata: meta}})
    }

    it(`it posts to workflow summary endpoint and renders workflow summary header`, (done) => {
        const wrapper = getWrapper(metaData);
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe('http://app/workflows/summary');
            expect(wrapper.find(runWorkflowSummaryHeader).props("workflowSummary")).toStrictEqual(summaryData);
            done();
        });
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