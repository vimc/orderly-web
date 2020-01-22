import {mount} from '@vue/test-utils';
import RemoveRole from "../../../js/components/permissions/removeRole.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";

describe("removeRole", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    const testRole = "TestRole";
    const testEmail = "test.user@test.com";
    const removeRoleUrl = `http://app/user-groups/${testRole}/user/${encodeURIComponent(testEmail)}`;

    it('sets error and does not emit removed event', (done) => {
        const testError = {test: "something"};
        mockAxios.onDelete(removeRoleUrl)
            .reply(500, testError);

        const wrapper = mount(RemoveRole, {
            propsData: {
                role: testRole,
                email: testEmail
            }
        });

        wrapper.find('.remove-user-group').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.delete.length).toBe(1);

            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe(`could not remove ${testEmail}`);
            expect(wrapper.find(ErrorInfo).props().apiError.response.data).toStrictEqual(testError);
            expect(wrapper.emitted().removed).toBeUndefined();
            done();
        });
    });

    it('removes user group and emits removed event', (done) => {
        mockAxios.onDelete(removeRoleUrl)
            .reply(200);

        const wrapper = mount(RemoveRole, {
            propsData: {
                role: testRole,
                email: testEmail
            }
        });

        wrapper.find('.remove-user-group').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.delete.length).toBe(1);
            expect(mockAxios.history.delete[0].url).toBe(removeRoleUrl);

            expect(wrapper.find(ErrorInfo).props().apiError).toBe(null);
            expect(wrapper.emitted().removed.length).toBe(1);
            expect(wrapper.emitted().removed[0]).toStrictEqual([]);

            done();
        });
    });

});