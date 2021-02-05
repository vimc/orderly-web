import {shallowMount} from "@vue/test-utils";
import ParameterList from "../../../js/components/runReport/parameterList.vue"


describe(`run report parameter list`, () => {
    const params = [
        {name: "global", default: "initial value"},
        {name: "minimal", default: "initial value 2"}
    ]

    const store = () => shallowMount(ParameterList,
        {
            propsData: {params: params}
        }
    )

    it(`can render parameter labels and values as expected`, () => {
        const wrapper = store()
        const labels = wrapper.find("table").findAll("label")

        expect(labels.at(0).text()).toBe("global")
        expect(labels.at(1).text()).toBe("minimal")
        expect(wrapper.vm.$data.paramValues).toBe(params)
    });

    it(`can render parameter values as expected`, () => {
        const wrapper = store()
        const inputs = wrapper.find("table").findAll("input")
        inputs.at(0).setValue("test Value1")
        inputs.at(1).setValue("test Value2")

        expect(wrapper.vm.$data.paramValues).toMatchObject([
            {name: "global", default: "test Value1"},
            {name: "minimal", default: "test Value2"}
        ])
    });

    it(`does not render ParameterList text control when no data to display`, () => {
        const wrapper = shallowMount(ParameterList)
        expect(wrapper.find("table").find("tr").exists()).toBe(false)
    });
});