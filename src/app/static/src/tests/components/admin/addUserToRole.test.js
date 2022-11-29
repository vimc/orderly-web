import {mount} from '@vue/test-utils';
import AddUserToRole from "../../../js/components/admin/addUserToRole.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";
import Vue from "vue";

describe("addUserToRole", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    function createSut() {
        return mount(AddUserToRole, {
            propsData: {
                role: "TestRole",
                availableUsers: ["a@test.com", "b@test.com"]
            }
        });
    }

    it('renders as expected', async () => {

        const wrapper = createSut();

        expect(wrapper.find('input').attributes("type")).toBe("search");
        expect(wrapper.find('button').text()).toBe("Add user");

        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBe("");
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("");
    });

    it('renders as expected with error', async () => {

        const wrapper = createSut();
        wrapper.setData({
            error: "test error",
            defaultMessage: "default error"
        });

        await Vue.nextTick();

        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBe("test error");
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("default error");

    });

    it('add sets error and does not emit added event', async () => {
        const testError = {test: "something"};
        mockAxios.onPost(`http://app/roles/TestRole/users/`)
            .reply(500, testError);

        const wrapper = createSut();

        wrapper.find('input').setValue('a@test.com');

        await Vue.nextTick();

        await wrapper.find('button').trigger('click');

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);

        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("could not add user");
        expect(wrapper.findComponent(ErrorInfo).props().apiError.response.data).toStrictEqual(testError);
        expect(wrapper.emitted().added).toBeUndefined();
    });

    it('add posts to role endpoint and emits added event', async () => {

        mockAxios.onPost(`http://app/roles/TestRole/users/`)
            .reply(200);

        const wrapper = createSut();

        wrapper.find("input").setValue('a@test.com');

        await Vue.nextTick();

        await wrapper.find('button').trigger('click');

        await Vue.nextTick();
        expect(wrapper.findAll('.text-danger').length).toBe(0);
        expect(mockAxios.history.post.length).toBe(1);

        const postData = JSON.parse(mockAxios.history.post[0].data);
        expect(postData).toStrictEqual({email: "a@test.com"});

        expect(wrapper.emitted().added.length).toBe(1);

    });

    it('validates that email value is an available user group', async () => {

        const wrapper = createSut();

        wrapper.find('input').setValue('notauser@test.com');

        await Vue.nextTick();

        await wrapper.find('button').trigger('click');

        await Vue.nextTick();
        expect(mockAxios.history.post.length).toBe(0);
        expect(wrapper.find('.text-danger').text()).toBe("Error: notauser@test.com is not an available user or already belongs to role");
    });

});