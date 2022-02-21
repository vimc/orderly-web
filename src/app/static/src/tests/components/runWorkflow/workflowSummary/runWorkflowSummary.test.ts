import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummary.vue"
import {RunWorkflowMetadata, WorkflowSummary} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";
import workflowSummaryReports from "../../../../js/components/runWorkflow/workflowSummary/workflowSummaryReports.vue";

describe(`runWorkflowSummary`, () => {

    const workflowSummary: WorkflowSummary = {
        ref: "test",
        missing_dependencies: {},
        reports: [{name: "test", param_list: [{name: "key", value: "value"}]}]
    }

    const metadata = {
        reports: [{name: "r1", params: {"key": "value"}}, {name: "r2"}],
        git_commit: "gitCommit"
    }

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onPost('http://app/workflows/summary/?commit=gitCommit')
            .reply(200, {"data": workflowSummary});
    })

    const getWrapper = (meta: Partial<RunWorkflowMetadata> = {reports: []}) => {
        return shallowMount(runWorkflowSummary,
            {
                propsData: {workflowMetadata: meta}
            })
    }

    it(`it can post workflow summary with dependencies as response`, (done) => {
        const wrapper = getWrapper(metadata);

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1)
            expect(mockAxios.history.post[0].url).toBe('http://app/workflows/summary/?commit=gitCommit');
            const data = JSON.parse(mockAxios.history.post[0].data);
            expect(data.ref).toEqual("gitCommit")
            expect(data.reports).toEqual(metadata.reports)
            expect(wrapper.findComponent(workflowSummaryReports).props("workflowSummary")).toEqual(workflowSummary)
            done()
        })
    });

    it(`emits valid event on mount`, () => {
        const wrapper = getWrapper({
            reports: [{name: "r1"}]
        });
        expect(wrapper.emitted().valid[0][0]).toBe(true);
    });

})