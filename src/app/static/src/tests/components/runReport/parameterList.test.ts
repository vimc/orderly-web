import {shallowMount} from "@vue/test-utils";
import ParameterList from "../../../js/components/runReport/parameterList.vue"
import {Parameters} from "../../../js/utils/types";


describe(`run report parameter list`, () => {
    const params = [
        {name: "global", default: "test"},
        {name: "minimal", default: ""}
        ]

    const store = () => shallowMount(ParameterList,
        {propsData: {params: params}})

    it(`can render ParameterList labels as expected`, () => {
        const wrapper = store()
        const labels = wrapper.find("table").findAll("label")

        expect(labels.at(0).text()).toBe("global")
        expect(labels.at(1).text()).toBe("minimal")
    });

    it(`can render ParameterList text control as expected`, () => {
        const wrapper = store()
        const inputs = wrapper.find("table").findAll("input")

        expect(inputs.at(0).exists()).toBe(true)
        expect(inputs.at(1).exists()).toBe(true)
    });

    it(`does not render ParameterList text control when no data to display`, () => {
        const wrapper = shallowMount(ParameterList)
        expect(wrapper.find("table").find("tr").exists()).toBe(false)
    });
});