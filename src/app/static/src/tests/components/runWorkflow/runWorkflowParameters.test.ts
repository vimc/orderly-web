import { shallowMount } from "@vue/test-utils";
import runWorkflowParameters from '../../../js/components/runWorkflow/runWorkflowParameters.vue'

describe(`runWorkflowParameters`, () => {

    const report1 = {
        name: "testReport",
        param_list: [{ name: "disease", value: "Measles" }],
        default_param_list: [{ name: "nmin", value: "123" }],
    }

    const report2 = {
        name: "testReport2",
        param_list: [],
        default_param_list: [{ name: "nmin2", value: "234" }, { name: "disease", value: "HepC" }]
    }

    const report3 = {
        name: "testReport2",
        param_list: [{ name: "nmin2", value: "345" }, { name: "disease", value: "Malaria" }],
        default_param_list: []
    }

    const getWrapper = (report = report1, reportIndex = 0) => {
        return shallowMount(runWorkflowParameters, { propsData: { report, reportIndex } })
    }
    it(`it can render default and non-default parameters`, () => {
        const wrapper = getWrapper();
        const params = wrapper.findAll(".non-default-param")
        expect(params.length).toBe(1);
        expect(params.at(0).text()).toBe("disease: Measles");
        const defaultParams1 = wrapper.findAll("#default-params-0 p");
        expect(defaultParams1.length).toBe(1);
        expect(defaultParams1.at(0).text()).toEqual("nmin: 123");

        const wrapper2 = getWrapper(report2, 1);
        expect(wrapper2.findAll(".non-default-param").length).toBe(0);
        expect(wrapper2.find("p").text()).toBe("No non-default parameters");
        const defaultParams2 = wrapper2.findAll("#default-params-1 p");
        expect(defaultParams2.at(0).text()).toEqual("nmin2: 234");
        expect(defaultParams2.at(1).text()).toEqual("disease: HepC");

        const wrapper3 = getWrapper(report3, 2);
        const params3 = wrapper3.findAll(".non-default-param")
        expect(params3.length).toBe(2);
        expect(params3.at(0).text()).toBe("nmin2: 345");
        expect(params3.at(1).text()).toBe("disease: Malaria");
        const defaultParams3 = wrapper.find("default-params-2");
        expect(defaultParams3.exists()).toBe(false);
    });

    it(`it can render placeholder text when no parameters to display`, () => {
        const wrapper = getWrapper({ name: "newReport", param_list: [], default_param_list: [] });
        const params = wrapper.findAll(".non-default-param")
        expect(params.length).toBe(0)
        expect(wrapper.text()).toBe("No parameters")
    });

    it("shows expand default parameters link only for reports with default parameters", () => {
        const wrapper = getWrapper();
        expect(wrapper.find("#default-params-0 b-link-stub.show-defaults .when-closed").text()).toBe("Show");
        expect(wrapper.find("#default-params-0 b-link-stub.show-defaults .when-open").text()).toBe("Hide");

        const wrapper2 = getWrapper(report2, 1);
        expect(wrapper2.find("#default-params-1 b-link-stub.show-defaults .when-closed").text()).toBe("Show");
        expect(wrapper2.find("#default-params-1 b-link-stub.show-defaults .when-open").text()).toBe("Hide");

        const wrapper3 = getWrapper(report3, 2);
        expect(wrapper3.find("#default-params-2 b-link-stub.show-defaults").exists()).toBe(false);

    });

})
