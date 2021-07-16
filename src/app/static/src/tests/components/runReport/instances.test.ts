import {shallowMount} from "@vue/test-utils";
import Vue from "vue";
import Instances from "../../../js/components/runReport/instances.vue"

describe(`instances`, () => {


    const getWrapper = (propsData: any = {}) => {
        return shallowMount(Instances, {
            propsData: {
                instances: {
                    source: ["prod", "uat"]
                },
                customStyle: {},
                ...propsData,
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

    it('emits selected value when an instance is selected', async() => {
        const wrapper = getWrapper()
        const options = wrapper.find("select").findAll("option")
        await options.at(0).setSelected()

        expect(wrapper.emitted().selectedValues.length).toBe(1)
        expect(wrapper.emitted().selectedValues[0][0]).toEqual({"source": "prod"})
    });

    it(`can set initial selected instances`, async () => {
        const wrapper = getWrapper({initialSelectedInstances: {source: "uat"}});
        expect(wrapper.vm.$data.selectedInstances).toStrictEqual({source: "uat"});
        await Vue.nextTick();
        expect((wrapper.find("select").element as HTMLSelectElement).value).toBe("uat")
    });

    it(`can set initial selected instances with defaults where no initial value provided`, async () => {
        const wrapper = getWrapper({
            instances: {
                annexe: ["annexe1", "annexe2"],
                source: ["prod", "uat"]
            },
            initialSelectedInstances: {source: "uat"}
        });
        expect(wrapper.vm.$data.selectedInstances).toStrictEqual({annexe: "annexe1", source: "uat"});
        await Vue.nextTick();
        const selects = wrapper.findAll("select");
        expect((selects.at(0).element as HTMLSelectElement).value).toBe("annexe1");
        expect((selects.at(1).element as HTMLSelectElement).value).toBe("uat");
    });
})
