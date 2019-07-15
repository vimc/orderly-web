import {mount} from '@vue/test-utils';
import RemovePermission from "../../../js/components/permissions/removePermission.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";

describe("removePermission", () => {


    beforeEach(() => {
        mockAxios.reset();
    });

    const testPermission = {
        name: "test.perm",
        scope_prefix: "report",
        scope_id: "r1"
    };

    it('sets error and does not emit removed event', (done) => {
        const testError = {test: "something"};
        mockAxios.onPost(`http://app/user-groups/Funder/actions/associate-permission/`)
            .reply(500, testError);

        const wrapper = mount(RemovePermission, {
            propsData: {
                permission: testPermission,
                userGroup: "Funder"
            }
        });

        wrapper.find('.remove-user-group').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);

            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("could not remove Funder");
            expect(wrapper.find(ErrorInfo).props().apiError.response.data).toStrictEqual(testError);
            expect(wrapper.emitted().removed).toBeUndefined();
            done();
        });
    });

    it('removes user group and emits removed event', (done) => {
        const testError = {test: "something"};
        mockAxios.onPost(`http://app/user-groups/Funder/actions/associate-permission/`)
            .reply(200);

        const wrapper = mount(RemovePermission, {
            propsData: {
                permission: testPermission,
                userGroup: "Funder"
            }
        });

        wrapper.find('.remove-user-group').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);

            expect(wrapper.find(ErrorInfo).props().apiError).toBe(null);
            expect(wrapper.emitted().removed).toBeDefined();

            const postData = JSON.parse(mockAxios.history.post[0].data);
            expect(postData.name).toBe("test.perm");
            expect(postData.action).toBe("remove");
            expect(postData.scope_prefix).toBe("report");
            expect(postData.scope_id).toBe("r1");

            done();
        });
    });

});