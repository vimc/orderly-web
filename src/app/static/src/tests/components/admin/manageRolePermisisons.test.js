import {shallowMount} from '@vue/test-utils';
import PermissionList from "../../../js/components/admin/permissionList.vue";
import ManageRolePermissions from "../../../js/components/admin/manageRolePermissions.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";
import Vue from "vue";

describe("manageRolePermissions", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    const mockRoles =  [
        {
            name: "Funders",
            permissions: [
                {
                    name: "reports.read",
                    scope_prefix: null,
                    scope_id: "*"
                }
            ]
        },
        {
            name: "Science",
            permissions: []
        }
    ];

    const getWrapper = function() {
        return shallowMount(ManageRolePermissions, {
            propsData: {roles: [{...mockRoles[0]}, {...mockRoles[1]}]}
        });
    };

    it('renders as expected', () => {
        const wrapper = getWrapper();

        const listItems = wrapper.findAll("li");
        expect(listItems.length).toBe(2);
        expect(listItems.at(0).find('span').text()).toBe("Funders");
        expect(listItems.at(0).find(PermissionList).props().permissions).toBe(mockRoles[0].permissions);
        expect(listItems.at(0).find(PermissionList).props().userGroup).toBe("Funders");

        expect(listItems.at(1).find('span').text()).toBe("Science");
        expect(listItems.at(1).findAll(PermissionList).length).toBe(0);

    });

    it('renders with error', async () => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });

        await Vue.nextTick();

        expect(wrapper.find(ErrorInfo).props().apiError).toBe("TEST ERROR");
        expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("TEST DEFAULT")
    });

    it('toggles list item', async () => {
        const wrapper = getWrapper();
        const firstRole = wrapper.find("li");

        firstRole.find(".expander").trigger("click");
        await Vue.nextTick();
        expect(wrapper.vm.$data.expanded[0]).toBe(true);

        firstRole.find(".role-name").trigger("click");
        await Vue.nextTick();
        expect(wrapper.vm.$data.expanded[0]).toBe(false);
    });

    it('posts to api, emits event and resets errors on successful remove', async (done) => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });
        await Vue.nextTick();

        const url = 'http://app/roles/Funders/permissions/reports.read/';
        mockAxios.onDelete(url)
            .reply(200);

        wrapper.find(PermissionList).vm.$emit("removed", mockRoles[0].permissions[0], "Funders");
        setTimeout(() => {
            expect(mockAxios.history.delete.length).toBe(1);
            expect(mockAxios.history.delete[0].url).toBe(url);

            expect(wrapper.emitted().changed.length).toBe(1);

            expect(wrapper.vm.$data.error).toBe(null);
            expect(wrapper.vm.$data.defaultMessage).toBe("Something went wrong");

            done();
        });
    });


    it('sets errors and does not emit event on unsuccessful remove', async (done) => {
        const wrapper = getWrapper();

        const url = 'http://app/roles/Funders/permissions/reports.read/';
        mockAxios.onDelete(url)
            .reply(500, "TEST API ERROR");

        wrapper.find(PermissionList).vm.$emit("removed", mockRoles[0].permissions[0], "Funders");
        setTimeout(() => {
            expect(mockAxios.history.delete.length).toBe(1);
            expect(mockAxios.history.delete[0].url).toBe(url);

            expect(wrapper.emitted().changed).toBe(undefined);

            expect(wrapper.vm.$data.error.response.data).toBe("TEST API ERROR");
            expect(wrapper.vm.$data.defaultMessage).toBe("could not remove permission from Funders");

            done();
        });
    });

    it('calls api with correct url when permission is scoped', async (done) => {
        const scopedRole = {
            name: "ScopedRole",
            permissions: [{name: "test.perm", scope_prefix: "report", scope_id: "r1"}]
        };

        const wrapper = shallowMount(ManageRolePermissions, {
            propsData: {roles: [scopedRole]}
        });

        wrapper.find(PermissionList).vm.$emit("removed", scopedRole.permissions[0], "ScopedRole");
        setTimeout(() => {
            expect(mockAxios.history.delete.length).toBe(1);
            const url = "http://app/roles/ScopedRole/permissions/test.perm/?scopePrefix=report&scopeId=r1";
            expect(mockAxios.history.delete[0].url).toBe(url);

            done();
        });
    });

    it('can add permission to role', async (done) => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });
        await Vue.nextTick();

        const url = 'http://app/roles/Funders/permissions/';
        mockAxios.onPost(url)
            .reply(200);

        wrapper.find(PermissionList).vm.$emit("added", "reports.review");
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe(url);

            expect(wrapper.find(ErrorInfo).props().apiError).toBeNull();
            expect(wrapper.emitted().changed.length).toBe(1);
            done();
        });
    });

    it('sets error if adding permission fails', async (done) => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });

        await Vue.nextTick();

        const url = 'http://app/roles/Funders/permissions/';
        mockAxios.onPost(url)
            .reply(500);

        wrapper.find(PermissionList).vm.$emit("added", "reports.review");

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe(url);

            expect(wrapper.emitted().changed).toBeUndefined();
            expect(wrapper.find(ErrorInfo).props().apiError).not.toBeNull();
            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("could not add reports.review to Funders");            done();
        });
    });
});