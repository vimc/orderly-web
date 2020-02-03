import {shallowMount} from '@vue/test-utils';
import ManageRolePermissions from "../../../js/components/admin/manageRolePermissions.vue";
import {mockAxios} from "../../mockAxios";
import RolePermissionsList from "../../../js/components/admin/rolePermissionsList.vue"
import Vue from "vue";

describe("manageRolePermissions", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/roles/')
            .reply(200, {"data": mockRoles});
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

    it('renders role permissions list', async () => {
        const wrapper = shallowMount(ManageRolePermissions);
        wrapper.setData({
            roles: mockRoles
        });

        await Vue.nextTick();
        const rolePermsList = wrapper.find(RolePermissionsList);

        expect(rolePermsList.props().roles).toBe(mockRoles);

    });

    it('fetches role on mount', async (done) => {
        const wrapper = shallowMount(ManageRolePermissions);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.find(RolePermissionsList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            done();
        });
    });

    it('refreshes roles when role permissions list emits removed event', async (done) => {
        const wrapper = shallowMount(ManageRolePermissions);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            wrapper.find(RolePermissionsList).vm.$emit("removed");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(2);
                expect(mockAxios.history.get[1].url).toBe("http://app/roles/");
                done();
            });
        });
    });

    it('refreshes roles when role permissions list emits added event', async (done) => {
        const wrapper = shallowMount(ManageRolePermissions);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            wrapper.find(RolePermissionsList).vm.$emit("added");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(2);
                expect(mockAxios.history.get[1].url).toBe("http://app/roles/");
                done();
            });
        });
    });
});