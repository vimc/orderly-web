import {shallowMount} from '@vue/test-utils';
import RolePermissionsList from "../../../js/components/admin/rolePermissionsList.vue";
import PermissionList from "../../../js/components/admin/permissionList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";
import Vue from "vue";

describe("rolePermissionsList", () => {
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
        return shallowMount(RolePermissionsList, {
            propsData: {roles: mockRoles}
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

            expect(wrapper.emitted().removed.length).toBe(1);
            expect(wrapper.emitted().removed[0][0]).toBe("Funders");
            expect(wrapper.emitted().removed[0][1]).toBe(mockRoles[0].permissions[0]);

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

            expect(wrapper.emitted().removed).toBe(undefined);

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

        const wrapper = shallowMount(RolePermissionsList, {
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
});