import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../js/components/runWorkflow/runWorkflowSummary.vue"
import runWorkflowSummaryHeader from "../../../js/components/runWorkflow/runWorkflowSummaryHeader.vue"
import {RunWorkflowMetadata} from "../../../js/utils/types";
import {mockAxios} from "../../mockAxios";

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
            const data = JSON.parse(mockAxios.history.post[0].data);
            expect(data.reports).toStrictEqual(metaData.reports);
            expect(data.ref).toStrictEqual(metaData.git_commit);
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

})