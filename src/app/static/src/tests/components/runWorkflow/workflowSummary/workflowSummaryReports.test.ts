import {shallowMount} from "@vue/test-utils";
import {WorkflowSummaryEndpoint} from "../../../../js/utils/types";
import workflowSummaryReports from "../../../../js/components/runWorkflow/workflowSummary/workflowSummaryReports.vue";

describe(`workflowSummaryReports`, () => {

    const workflowSummary = {
        ref: "commit123",
        missing_dependencies: {},
        reports: [
            {name: "testReport", params: {"nmin": "1"}},
            {name: "testReport2", params: {"nmin": "2"}}
        ]
    }

    const mockTooltip = jest.fn();

    const getWrapper = (summary: Partial<WorkflowSummaryEndpoint> = {}) => {
        return shallowMount(workflowSummaryReports,
            {
                propsData: {
                    workflowSummary: summary
                },
                directives: {"tooltip": mockTooltip}
            })
    }

    it(`it can render tooltip text for reports that run single and multiple times`,  () => {
        const sameReports = [{name: "testReport"}, {name: "testReport"}, {name: "testReport2"}]
        const wrapper = getWrapper( {reports:sameReports});

        expect(wrapper.find("#workflow-summary").exists()).toBe(true)
        const reports = wrapper.findAll("#report-name-icon")
        expect(reports.length).toBe(3)

        expect(reports.at(0).find("h5").text()).toBe("testReport")
        expect(mockTooltip.mock.calls[0][1].value).toEqual("testReport runs 2 times")

        expect(reports.at(2).find("h5").text()).toBe("testReport2")
        expect(mockTooltip.mock.calls[2][1].value).toEqual("testReport2 runs 1 time")
    });

    it(`it can render report name and info icon`,  () => {
        const wrapper = getWrapper(workflowSummary);

        expect(wrapper.find("#workflow-summary").exists()).toBe(true)
        const reports = wrapper.findAll("#report-name-icon")
        expect(reports.length).toBe(2)

        expect(reports.at(0).find("h5").text()).toBe("testReport")
        expect(reports.at(0).find("info-icon-stub").exists()).toBeTruthy()
        expect(reports.at(1).find("info-icon-stub").attributes()).toEqual({
            class: "custom-class",
            size: "1.2x",
            stroke: "grey"
        })

        expect(reports.at(1).find("h5").text()).toBe("testReport2")
        expect(reports.at(1).find("info-icon-stub").exists()).toBeTruthy()
        expect(reports.at(1).find("info-icon-stub").attributes()).toEqual({
            class: "custom-class",
            size: "1.2x",
            stroke: "grey"
        })
    });

    it(`it can render report parameters`,  () => {
        const wrapper = getWrapper(workflowSummary);
        const parametersHeading = wrapper.find("#report-params span")
        expect(parametersHeading.text()).toBe("Parameters")

        const params = wrapper.findAll("#params")
        expect(params.length).toBe(2)
        expect(params.at(0).text()).toBe("nmin: 1")
        expect(params.at(1).text()).toBe("nmin: 2")
    });

    it(`it can render placeholder text when no parameters to display`,  () => {
        const wrapper = getWrapper({reports: [{name: "new report"}]});
        const params = wrapper.findAll("#params")
        expect(params.length).toBe(0)
        expect(wrapper.find("#report-params p").text()).toBe("There are no parameters")
    });

})