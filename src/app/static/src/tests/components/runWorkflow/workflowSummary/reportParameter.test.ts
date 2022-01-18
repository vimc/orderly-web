import {shallowMount} from "@vue/test-utils";
import {Dependency} from "../../../../js/utils/types";
import {mockAxios} from "../../../mockAxios";
import reportParameter from "../../../../js/components/runWorkflow/workflowSummary/reportParameter.vue";

describe(`reportParameter`, () => {

    const dependency = {
        refs: "commit123",
        missing_dependencies: {},
        reports: [
            {name: "testReport", params: {"nmin": "1"}},
            {name: "testReport2", params: {"nmin": "2"}}
        ]
    }

    const defaultParameters = [{"name": "nmin", "value": "default"},{"name": "nmin", "value": "123"}]
    const defaultParameters2 = [{"name": "nmin2", "value": "default2"},{"name": "nmin2", "value": "123"}]

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report/testReport/config/parameters/?commit=commit123')
            .reply(200, {"data": defaultParameters});
        mockAxios.onGet('http://app/report/testReport2/config/parameters/?commit=commit123')
            .reply(200, {"data": defaultParameters2});
    })

    const getWrapper = (dependency: Partial<Dependency> = {}) => {
        return shallowMount(reportParameter,
            {
                propsData: {
                    dependencies: dependency,
                    gitCommit: "commit123"
                }
            })
    }

    it(`it can render report name and icon`,  () => {
        const wrapper = getWrapper(dependency);

        expect(wrapper.find("#workflow-summary").exists()).toBe(true)
        const reports = wrapper.findAll("#report-name-icon")
        expect(reports.length).toBe(2)

        expect(reports.at(0).find("h5").text()).toBe("testReport")
        expect(reports.at(0).find("info-icon-stub").exists()).toBeTruthy()
        expect(reports.at(1).find("info-icon-stub").attributes()).toEqual({
            class: "custom-class has-tooltip",
            "data-original-title": "null",
            size: "1.2x",
            stroke: "grey"
        })

        expect(reports.at(1).find("h5").text()).toBe("testReport2")
        expect(reports.at(1).find("info-icon-stub").exists()).toBeTruthy()
        expect(reports.at(1).find("info-icon-stub").attributes()).toEqual({
            class: "custom-class has-tooltip",
            "data-original-title": "null",
            size: "1.2x",
            stroke: "grey"
        })
    });

    it(`it can render report parameters`,  () => {
        const wrapper = getWrapper(dependency);
        const parametersHeading = wrapper.find("#report-params span")
        expect(parametersHeading.text()).toBe("Parameters")

        const params = wrapper.findAll("#params")
        expect(params.length).toBe(2)
        expect(params.at(0).text()).toBe("nmin: 1")
        expect(params.at(1).text()).toBe("nmin: 2")
    });

    it(`it can render default text when no parameters`,  () => {
        const wrapper = getWrapper({reports: [{name: "new report"}]});
        const params = wrapper.findAll("#params")
        expect(params.length).toBe(0)
        expect(wrapper.find("#report-params p").text()).toBe("There are no parameters")
    });

    it(`it can get default parameters`,  (done) => {
        const wrapper = getWrapper(dependency);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2)
            expect(mockAxios.history.get[0].url).toBe('http://app/report/testReport/config/parameters/?commit=commit123');
            expect(mockAxios.history.get[1].url).toBe('http://app/report/testReport2/config/parameters/?commit=commit123');
            expect(wrapper.vm.$data.defaultParamsError).toStrictEqual([])
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
            .reply(404, "ERROR");

        const wrapper = getWrapper(dependency);

        setTimeout(() => {
            expect(wrapper.vm.$data.defaultParamsError.length).toBe(1)
            expect(wrapper.vm.$data.defaultParamsError[0].reportName).toBe("testReport")
            expect(wrapper.vm.$data.defaultParamsError[0].error.message).toStrictEqual("Request failed with status code 404")
            done()
        })
    });

    it(`it can render default parameters`, (done) => {
        const wrapper = getWrapper(dependency);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2)
            expect(wrapper.vm.$data.defaultParams.length).toBe(2)
            const defaultParams = wrapper.findAll("#default-params-collapse")
            expect(defaultParams.length).toBe(4)
            expect(defaultParams.at(0).text()).toBe("nmin: default")
            expect(defaultParams.at(1).text()).toBe("nmin: 123")
            expect(defaultParams.at(2).text()).toBe("nmin2: default2")
            expect(defaultParams.at(3).text()).toBe("nmin2: 123")
            done()
        })
    });

})