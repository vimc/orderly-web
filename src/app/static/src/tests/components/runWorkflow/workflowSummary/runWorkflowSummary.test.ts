import {shallowMount} from "@vue/test-utils";
import runWorkflowSummary from "../../../../js/components/runWorkflow/workflowSummary/runWorkflowSummary.vue"
import {RunWorkflowMetadata} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";
import reportParameter from "../../../../js/components/runWorkflow/workflowSummary/reportParameter.vue";

describe(`runWorkflowSummary`, () => {

    const dependency = {
        refs: "test",
        missing_dependencies: {},
        reports: [{name: "test", params: {"key": "value"}}]
    }

    const metadata = {
        reports: [{name: "r1", params: {"key": "value"}}, {name: "r2"}],
        git_commit: "gitCommit"
    }

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onPost('http://app/workflows/summary')
            .reply(200, {"data": dependency});
    })

    const getWrapper = (meta: Partial<RunWorkflowMetadata> = {reports: []}) => {
        return shallowMount(runWorkflowSummary,
            {
                propsData: {workflowMetadata: meta}
            })
    }

    it(`it can post workflow summary with dependencies as response`,  (done) => {
        const wrapper = getWrapper(metadata);

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1)
            expect(mockAxios.history.post[0].url).toBe('http://app/workflows/summary');
            const data = JSON.parse(mockAxios.history.post[0].data);
            expect(data.ref).toEqual("gitCommit")
            expect(data.reports).toEqual(metadata.reports)
            expect(wrapper.findComponent(reportParameter).props("dependencies")).toEqual(dependency)
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