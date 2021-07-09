import {shallowMount} from "@vue/test-utils";
import Instances from "../../../js/components/runReport/instances.vue"

describe(`instances`, () => {


    const getWrapper = () => {
        return shallowMount(Instances, {
            propsData: {
                instances: {
                    source: ["prod", "uat"]
                },
                customStyle: {}
            }
        })
    }

    it('renders component correctly', () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#instances-div label").text()).toBe("Database \"source\"");
        expect(wrapper.find("#source").exists()).toBe(true);

        const sourceOptions = wrapper.findAll("#source option");
        expect(sourceOptions.length).toBe(2);
        expect(sourceOptions.at(0).attributes().value).toBe("prod");
        expect(sourceOptions.at(0).text()).toBe("prod");
        expect(sourceOptions.at(1).attributes().value).toBe("uat");
        expect(sourceOptions.at(1).text()).toBe("uat");
    });

    it('renders styles correctly when custom style prop is set', async() => {
        const label = ["col-form-label", "col-sm-2", "text-right"]
        const control = ["col-sm-6"]

        const wrapper = getWrapper()
        await wrapper.setProps({customStyle: {label: "col-sm-2 text-right", control: "col-sm-6"}})

        const instances = wrapper.find("#instances-div")
        expect(instances.find("label").classes()).toEqual(label)
        expect(wrapper.find("#instance-control").classes()).toEqual(control)
    });

    it('emits selected value and clear run when an instance is selected', async() => {
        const wrapper = getWrapper()
        const options = wrapper.find("select").findAll("option")
        await options.at(0).setSelected()

        expect(wrapper.emitted().clearRun.length).toBe(1)
        expect(wrapper.emitted().selectedValues.length).toBe(1)
        expect(wrapper.emitted().selectedValues[0][0]).toEqual({"source": "prod"})
    });

})