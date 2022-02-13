import {shallowMount} from "@vue/test-utils";
import {WorkflowSummary} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";
import runWorkflowMixin from "../../../../js/components/runWorkflow/workflowParametersMixin";
import ErrorInfo from "../../../../js/components/errorInfo.vue";
import workflowSummaryReports from "../../../../js/components/runWorkflow/workflowSummary/workflowSummaryReports.vue";

describe(`workflowSummaryReports`, () => {

    const workflowSummary = {
        ref: "commit123",
        missing_dependencies: {},
        reports: [
            {name: "testReport", params: {nmin: "123", disease: "Measles"}}, //report with both default and non-default params
            {name: "testReport2", params: {nmin2: "234", disease: "HepC"}}, //report with default params only
            {name: "testReport2", params: {nmin2: "345", disease: "Malaria"}}, //report with non-default parans

        ]
    }

    const defaultParameters = [{"name": "nmin", "value": "123"}, {"name": "disease", "value": "HepB"}];
    const defaultParameters2 = [{"name": "nmin2", "value": "234"}, {"name": "disease", "value": "HepC"}];

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

    it(`it can render tooltip text for reports that run single and multiple times`, (done) => {
        setTimeout(() => {
            const sameReports = [
                {name: "testReport", params: {}},
                {name: "testReport", params: {}},
                {name: "testReport2", params: {}}
            ];
            const wrapper = getWrapper({reports: sameReports});

            expect(wrapper.find("#workflow-summary").exists()).toBe(true)
            const reports = wrapper.findAll("#report-name-icon")
            expect(reports.length).toBe(3)

            expect(reports.at(0).find("h5").text()).toBe("testReport")
            expect(mockTooltip.mock.calls[0][1].value).toEqual("testReport runs 2 times")

            expect(reports.at(2).find("h5").text()).toBe("testReport2")
            expect(mockTooltip.mock.calls[2][1].value).toEqual("testReport2 runs 1 time")

            done();
        });
    });

    it(`it can render report name and info icon`, (done) => {
        const wrapper = getWrapper(workflowSummary);

        setTimeout(() => {
            expect(wrapper.find("#workflow-summary").exists()).toBe(true)
            const reports = wrapper.findAll("#report-name-icon")
            expect(reports.length).toBe(3)

            expect(reports.at(0).find("h5").text()).toBe("testReport")
            expect(reports.at(1).find("h5").text()).toBe("testReport2")
            expect(reports.at(2).find("h5").text()).toBe("testReport2")

            const indexes = [0, 1, 2];
            indexes.forEach(idx => {
                expect(reports.at(idx).find("info-icon-stub").exists()).toBeTruthy()
                expect(reports.at(idx).find("info-icon-stub").attributes()).toEqual({
                    class: "custom-class",
                    size: "1.2x",
                    stroke: "grey"
                })
            });

            done();
        });
    });

    it(`it can render placeholder text when no parameters to display`, (done) => {
        mockAxios.onGet('http://app/report/newReport/config/parameters/?commit=commit123')
            .reply(200, {"data": []});

        const wrapper = getWrapper({reports: [{name: "newReport", params: {}}]});
        setTimeout(() => {
            const params = wrapper.findAll(".non-default-param")
            expect(params.length).toBe(0)
            expect(wrapper.find(".report-params p").text()).toBe("There are no parameters")

            //check no attempt to fetch default parameters
            expect(mockAxios.history.get.length).toBe(0);

            done();
        });
    });

    it(`it can get default parameters`, (done) => {
        const wrapper = getWrapper(workflowSummary);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(mockAxios.history.get[0].url).toBe('http://app/report/testReport/config/parameters/?commit=commit123');
            expect(mockAxios.history.get[1].url).toBe('http://app/report/testReport2/config/parameters/?commit=commit123');
            expect(wrapper.vm.$data.defaultParamsErrors).toStrictEqual({});
            expect(wrapper.vm.$data.workflowReportParams.length).toBe(3);
            expect(wrapper.vm.$data.workflowReportParams[0]).toEqual(
                {
                    defaultParams: [{name: "nmin", value: "123"}],
                    nonDefaultParams: [{name: "disease", value: "Measles"}]
                });
            expect(wrapper.vm.$data.workflowReportParams[1]).toEqual(
                {
                    defaultParams: [{name: "nmin2", value: "234"}, {name: "disease", value: "HepC"}],
                    nonDefaultParams: []
                });
            expect(wrapper.vm.$data.workflowReportParams[2]).toEqual(
                {
                    defaultParams: [],
                    nonDefaultParams: [{name: "nmin2", value: "345"}, {name: "disease", value: "Malaria"}]
                });
            done();
        })
    });

    it(`it can render default parameters error`, (done) => {
        mockAxios.onGet('http://app/report/testReport/config/parameters/?commit=commit123')
            .reply(404, "TEST ERROR");

        const wrapper = getWrapper({reports: [{name: "testReport", params: {"nmin": "test"}}]});

        setTimeout(() => {
            const errors = wrapper.vm.$data.defaultParamsErrors;
            expect(Object.keys(errors).length).toBe(1);
            expect(errors["testReport"].message).toBe("Request failed with status code 404")

            expect(wrapper.findComponent(ErrorInfo).props("apiError").message)
                .toBe("Request failed with status code 404");

            expect(wrapper.findComponent(ErrorInfo).props("defaultMessage"))
                .toBe("An error occurred while retrieving default params");
            done();
        })
    });

    it(`it can render non-default parameters`, (done) => {
        const wrapper = getWrapper(workflowSummary);
        setTimeout(() => {
            const parametersHeading = wrapper.find(".report-params span");
            expect(parametersHeading.text()).toBe("Parameters");

            const reportRows = wrapper.findAll(".report-params");

            //report 1
            let params = reportRows.at(0).findAll(".non-default-param");
            expect(params.length).toBe(1);
            expect(params.at(0).text()).toBe("disease: Measles");

            //report 2
            params = reportRows.at(1).findAll(".non-default-param");
            expect(params.length).toBe(0);

            //report 3
            params = reportRows.at(2).findAll(".non-default-param");
            expect(params.length).toBe(2);
            expect(params.at(0).text()).toBe("nmin2: 345");
            expect(params.at(1).text()).toBe("disease: Malaria");

            done();
        });
    });

    it(`it can render default parameters`, (done) => {
        const wrapper = getWrapper(workflowSummary);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.vm.$data.workflowReportParams.length).toBe(3);

            const reportRows = wrapper.findAll(".report-params");

            //report 1
            const defaultParams1 = wrapper.findAll("#default-params-0 p");
            expect(defaultParams1.length).toBe(1);
            expect(defaultParams1.at(0).text()).toEqual("nmin: 123");
            expect(reportRows.at(0).find(ErrorInfo).props().apiError).toBeNull();
            expect(reportRows.at(0).find(ErrorInfo).props().defaultMessage).toBeNull();


            //report 2
            const defaultParams2 = wrapper.findAll("#default-params-1 p");
            expect(defaultParams2.at(0).text()).toEqual("nmin2: 234");
            expect(defaultParams2.at(1).text()).toEqual("disease: HepC");
            expect(reportRows.at(1).find(ErrorInfo).props().apiError).toBeNull();
            expect(reportRows.at(1).find(ErrorInfo).props().defaultMessage).toBeNull();

            //report 3 - no defaults
            const defaultParams3 = wrapper.find("default-params-2");
            expect(defaultParams3.exists()).toBe(false);
            expect(reportRows.at(2).find(ErrorInfo).props().apiError).toBeNull();
            expect(reportRows.at(2).find(ErrorInfo).props().defaultMessage).toBeNull();

            done()
        })
    });

    it("shows expand default parameters link only for reports with default parameters", (done) => {
        const wrapper = getWrapper(workflowSummary);

        setTimeout(() => {
            expect(wrapper.find("#default-params-0 b-link-stub.show-defaults .when-closed").text()).toBe("Show");
            expect(wrapper.find("#default-params-0 b-link-stub.show-defaults .when-open").text()).toBe("Hide");

            expect(wrapper.find("#default-params-1 b-link-stub.show-defaults .when-closed").text()).toBe("Show");
            expect(wrapper.find("#default-params-1 b-link-stub.show-defaults .when-open").text()).toBe("Hide");

            expect(wrapper.find("#default-params-2 b-link-stub.show-defaults").exists()).toBe(false);
            done();
        });
    });

});
