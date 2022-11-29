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

    const mockRoles = [
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

    const getWrapper = function () {
        return shallowMount(ManageRolePermissions, {
            propsData: {roles: [{...mockRoles[0]}, {...mockRoles[1]}]}
        });
    };

    it('renders as expected', () => {
        const wrapper = getWrapper();

        const listItems = wrapper.findAll("li");
        const permissionLists = wrapper.findAllComponents(PermissionList);
        expect(listItems.length).toBe(2);
        expect(permissionLists.length).toBe(2);
        expect(listItems.at(0).find('span').text()).toBe("Funders");
        expect(permissionLists.at(0).props().permissions).toBe(mockRoles[0].permissions);
        expect(permissionLists.at(0).props().userGroup).toBe("Funders");

        expect(listItems.at(1).find('span').text()).toBe("Science");
        expect(permissionLists.at(1).props().permissions.length).toBe(0);
        expect(permissionLists.at(1).props().canEdit).toBe(true);

    });

    it('renders PermissionList with canEdit false for Admin role', () => {
        const wrapper = shallowMount(ManageRolePermissions, {
            propsData: {
                roles: [
                    {name: "Admin", permissions: []}
                ]
            }
        });

        const listItems = wrapper.findAllComponents(PermissionList);
        expect(listItems.length).toBe(1);
        expect(listItems.at(0).props().canEdit).toBe(false);
    });

    it('renders with error', async () => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });

        await Vue.nextTick();

        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBe("TEST ERROR");
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("TEST DEFAULT")
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

    it('posts to api, emits event and resets errors on successful remove', async () => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });
        await Vue.nextTick();

        const url = 'http://app/roles/Funders/permissions/reports.read/';
        mockAxios.onDelete(url)
            .reply(200);

        wrapper.findComponent(PermissionList).vm.$emit("removed", mockRoles[0].permissions[0], "Funders");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.delete.length).toBe(1);
        expect(mockAxios.history.delete[0].url).toBe(url);

        expect(wrapper.emitted().changed.length).toBe(1);

        expect(wrapper.vm.$data.error).toBe(null);
        expect(wrapper.vm.$data.defaultMessage).toBe("Something went wrong");
    });


    it('sets errors and does not emit event on unsuccessful remove', async () => {
        const wrapper = getWrapper();

        const url = 'http://app/roles/Funders/permissions/reports.read/';
        mockAxios.onDelete(url)
            .reply(500, "TEST API ERROR");

        await wrapper.findComponent(PermissionList).vm.$emit("removed", mockRoles[0].permissions[0], "Funders");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.delete.length).toBe(1);
        expect(mockAxios.history.delete[0].url).toBe(url);

        expect(wrapper.emitted().changed).toBe(undefined);
        expect(wrapper.vm.$data.error.response.data).toBe("TEST API ERROR");
        expect(wrapper.vm.$data.defaultMessage).toBe("could not remove permission from Funders");
    });

    it('calls api with correct url when permission is scoped', async () => {
        const scopedRole = {
            name: "ScopedRole",
            permissions: [{name: "test.perm", scope_prefix: "report", scope_id: "r1"}]
        };

        const wrapper = shallowMount(ManageRolePermissions, {
            propsData: {roles: [scopedRole]}
        });

        await wrapper.findComponent(PermissionList).vm.$emit("removed", scopedRole.permissions[0], "ScopedRole");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.delete.length).toBe(1);
        const url = "http://app/roles/ScopedRole/permissions/test.perm/?scopePrefix=report&scopeId=r1";
        expect(mockAxios.history.delete[0].url).toBe(url);
    });

    it('can add permission to role', async () => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });
        await Vue.nextTick();

        const url = 'http://app/roles/Funders/permissions/';
        mockAxios.onPost(url)
            .reply(200);

        wrapper.findComponent(PermissionList).vm.$emit("added", "reports.review");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);
        expect(mockAxios.history.post[0].url).toBe(url);

        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBeNull();
        expect(wrapper.emitted().changed.length).toBe(1);
    });

    it('sets error if adding permission fails', async () => {
        const wrapper = getWrapper();
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });

        await Vue.nextTick();

        const url = 'http://app/roles/Funders/permissions/';
        mockAxios.onPost(url)
            .reply(500);

        await wrapper.findComponent(PermissionList).vm.$emit("added", "reports.review");

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);
        expect(mockAxios.history.post[0].url).toBe(url);

        expect(wrapper.emitted().changed).toBeUndefined();
        expect(wrapper.findComponent(ErrorInfo).props().apiError).not.toBeNull();
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("could not add reports.review to Funders");
    });
});