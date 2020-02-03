import {shallowMount} from '@vue/test-utils';
import {mockAxios} from "../../mockAxios";
import ManageRolePermissions from "../../../js/components/admin/manageRolePermissions.vue";
import ManageRoles from "../../../js/components/admin/manageRoles.vue";
import ManageUserPermissions from "../../../js/components/admin/manageUserPermissions.vue";
import AdminApp from "../../../js/components/admin/adminApp.vue";
import Vue from "vue";

describe("adminApp", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/roles/')
            .reply(200, {"data": mockRoles});
        mockAxios.onGet('http://app/users/')
            .reply(200, {"data": mockUsers});
    });

    const mockUsers = [
        {
            username: "a.user",
            display_name: "Some name",
            email: "a@example.com",
            direct_permissions: [],
            role_permissions: []
        },
        {
            username: "b.user",
            display_name: "Some other name",
            email: "b@example.com",
            direct_permissions: [{
                name: "reports.read",
                scope_id: "",
                scope_prefix: null
            }],
            role_permissions: []
        }
    ];


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

    it('renders components', async () => {
        const wrapper = shallowMount(AdminApp);
        wrapper.setData({
            roles: mockRoles,
            users: mockUsers
        });

        await Vue.nextTick();
        expect(wrapper.find(ManageRoles).props().roles).toBe(mockRoles);
        expect(wrapper.find(ManageUserPermissions).props().allUsers).toBe(mockUsers);
        expect(wrapper.find(ManageRolePermissions).props().roles).toBe(mockRoles);
    });

    it('fetches roles and users on mount', async (done) => {
        const wrapper = shallowMount(AdminApp);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.vm.$data.roles).toEqual(expect.arrayContaining(mockRoles));
            expect(wrapper.vm.$data.users).toEqual(expect.arrayContaining(mockUsers));
            done();
        });
    });

    it('refreshes roles and users when role permissions list emits changed event', async (done) => {
        const wrapper = shallowMount(AdminApp);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);

            wrapper.find(ManageRolePermissions).vm.$emit("changed");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(4);
                expect(mockAxios.history.get[2].url).toBe("http://app/roles/");
                expect(mockAxios.history.get[3].url).toBe("http://app/users/");
                done();
            });
        });
    });

    it('refreshes roles and users when manage roles emits changed event', async (done) => {
        const wrapper = shallowMount(AdminApp);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);

            wrapper.find(ManageRoles).vm.$emit("changed");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(4);
                expect(mockAxios.history.get[2].url).toBe("http://app/roles/");
                expect(mockAxios.history.get[3].url).toBe("http://app/users/");
                done();
            });
        });
    });
    
});