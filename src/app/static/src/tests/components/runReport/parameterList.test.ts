import {shallowMount} from "@vue/test-utils";
import ParameterList from "../../../js/components/runReport/parameterList.vue"


describe(`run report parameter list`, () => {
    const params = [
        {name: "global", value: "initial value"},
        {name: "minimal", value: "initial value 2"}
    ]

    const store = () => shallowMount(ParameterList,
        {
            propsData: {params: params}
        }
    )

    it(`can render parameter labels, values and props as expected`, () => {
        const wrapper = store()
        const labels = wrapper.find("table").findAll("label")

        expect(labels.at(0).text()).toBe("global")
        expect(labels.at(1).text()).toBe("minimal")

        const inputs = wrapper.find("table").findAll("input")
        const globalInput = inputs.at(0).element as HTMLInputElement
        expect(globalInput.value).toBe("initial value")
        const minimalInput = inputs.at(1).element as HTMLInputElement
        expect(minimalInput.value).toBe("initial value 2")
    });

    it(`can emmit on mount and render parameter error as expected`, async() => {
        const innerParams = [
            {name: "global", value: ""},
            {name: "minimal", value: "initial value 2"}
        ]
        const wrapper = shallowMount(ParameterList,
            {
                propsData: {params: innerParams}
            }
        )
        expect(wrapper.emitted().getParams.length).toBe(1)
        expect(wrapper.vm.$props.error).toBe("Parameter value(s) required")
    });

    it(`can render parameter values as expected`, () => {
        const wrapper = store()
        const inputs = wrapper.find("table").findAll("input")
        inputs.at(0).setValue("test Value1")
        inputs.at(1).setValue("test Value2")

        expect(wrapper.vm.$data.paramValues).toMatchObject([
            {name: "global", value: "test Value1"},
            {name: "minimal", value: "test Value2"}
        ])
    });

    it(`does not render ParameterList text control when no data to display`, () => {
        const wrapper = shallowMount(ParameterList)
        expect(wrapper.find("table").find("tr").exists()).toBe(false)
    });
});