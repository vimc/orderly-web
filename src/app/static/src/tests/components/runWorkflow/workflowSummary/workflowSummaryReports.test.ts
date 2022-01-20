import {shallowMount} from "@vue/test-utils";
import {WorkflowSummary} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";
import runWorkflowMixin from "../../../../js/components/runWorkflow/runWorkflowMixin";
import ErrorInfo from "../../../../js/components/errorInfo.vue";
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

    const defaultParameters = [{"name": "nmin", "value": "default"}, {"name": "nmin", "value": "123"}]
    const defaultParameters2 = [{"name": "nmin2", "value": "default2"}, {"name": "nmin2", "value": "123"}]

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report/testReport/config/parameters/?commit=commit123')
            .reply(200, {"data": defaultParameters});
        mockAxios.onGet('http://app/report/testReport2/config/parameters/?commit=commit123')
            .reply(200, {"data": defaultParameters2});
    })

    const mockTooltip = jest.fn();

    const getWrapper = (summary: Partial<WorkflowSummary> = {}) => {
        return shallowMount(workflowSummaryReports,
            {
                propsData: {
                    workflowSummary: summary,
                    gitCommit: "commit123"
                },
                mixins: [runWorkflowMixin],
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

    it(`it can get default parameters`, (done) => {
        const wrapper = getWrapper(workflowSummary);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2)
            expect(mockAxios.history.get[0].url).toBe('http://app/report/testReport/config/parameters/?commit=commit123');
            expect(mockAxios.history.get[1].url).toBe('http://app/report/testReport2/config/parameters/?commit=commit123');
            expect(wrapper.vm.$data.defaultParamsErrors).toStrictEqual([])
            expect(wrapper.vm.$data.defaultParams.length).toBe(2)
            expect(wrapper.vm.$data.defaultParams[0]).toEqual(
                {
                    "params": [{"name": "nmin", "value": "default"}, {"name": "nmin", "value": "123"}],
                    "reportName": "testReport"
                })
            expect(wrapper.vm.$data.defaultParams[1]).toEqual(
                {
                    "params": [{"name": "nmin2", "value": "default2"}, {"name": "nmin2", "value": "123"}],
                    "reportName": "testReport2"
                })
            done()
        })
    });

    it(`it can render default parameters error`, (done) => {
        mockAxios.onGet('http://app/report/testReport/config/parameters/?commit=commit123')
            .reply(404, "TEST ERROR");

        const wrapper = getWrapper({reports: [{name: "testReport", params: {"nmin": "test"}}]});

        setTimeout(() => {
            expect(wrapper.vm.$data.defaultParamsErrors.length).toBe(1)
            expect(wrapper.vm.$data.defaultParamsErrors[0].reportName).toBe("testReport")
            expect(wrapper.vm.$data.defaultParamsErrors[0].error.message)
                .toStrictEqual("Request failed with status code 404")

            expect(wrapper.findComponent(ErrorInfo).props("apiError").error.message)
                .toBe("Request failed with status code 404");

            expect(wrapper.findComponent(ErrorInfo).props("defaultMessage"))
                .toBe("An error occurred while retrieving default params");
            done();
        })
    });

    it(`it can render default parameters`, (done) => {
        const wrapper = getWrapper(workflowSummary);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2)
            expect(wrapper.vm.$data.defaultParams.length).toBe(2)

            //row1
            const defaultParamOne = wrapper.find("#default-params-0")
            const defaultParamsCollapseOne = defaultParamOne.findAll("#default-params-collapse")
            expect(defaultParamsCollapseOne.length).toBe(2)
            expect(defaultParamsCollapseOne.at(0).text()).toBe("nmin: default")
            expect(defaultParamsCollapseOne.at(1).text()).toBe("nmin: 123")

            //row2
            const defaultParamTwo = wrapper.find("#default-params-1")
            const defaultParamsCollapseTwo = defaultParamTwo.findAll("#default-params-collapse")
            expect(defaultParamsCollapseTwo.length).toBe(2)
            expect(defaultParamsCollapseTwo.at(0).text()).toBe("nmin2: default2")
            expect(defaultParamsCollapseTwo.at(1).text()).toBe("nmin2: 123")
            done()
        })
    });

})