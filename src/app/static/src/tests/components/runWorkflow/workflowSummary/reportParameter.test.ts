import {shallowMount} from "@vue/test-utils";
import {Dependency} from "../../../../js/utils/types";
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


    it(`it can render default parameters`, () => {
        const wrapper = getWrapper(dependency);
        const defaultParams = wrapper.findAll("#default-params-collapse")
        expect(defaultParams.length).toBe(2)
        expect(defaultParams.at(0).text()).toBe("nmin: 1")
        expect(defaultParams.at(1).text()).toBe("nmin: 2")
    });

})