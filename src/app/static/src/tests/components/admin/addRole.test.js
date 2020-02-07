import {shallowMount} from '@vue/test-utils';
import AddRole from "../../../js/components/admin/addRole.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";
import Vue from "vue"

describe("addRole", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    const createSut = () => {
        return shallowMount(AddRole, {
            propsData: {
            }
        });
    };

    it('renders as expected', () => {
        const wrapper = createSut();

        expect(wrapper.find('input').attributes("type")).toBe("text");
        expect(wrapper.find('button').text()).toBe("Add role");

    });

    it ('emits added event when button clicked', async () => {
        const wrapper = createSut();

        wrapper.find("input").setValue('NewRole');
        await Vue.nextTick();

        wrapper.find('button').trigger('click');

        expect(wrapper.emitted().added.length).toBe(1);
        expect(wrapper.emitted().added[0][0]).toBe('NewRole');
    });

    it('button is disabled if no value in input', () => {
        const wrapper = createSut();
        expect(wrapper.find("button").attributes().disabled).toBe("disabled");
    });

    it('button is not disabled if value in input', async () => {
        const wrapper = createSut();

        wrapper.find("input").setValue('NewRole');
        await Vue.nextTick();

        expect(wrapper.find("button").attributes().disabled).toBe(undefined);
    });
});