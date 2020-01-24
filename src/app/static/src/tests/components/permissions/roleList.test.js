import {shallowMount} from '@vue/test-utils';
import RoleList from "../../../js/components/permissions/roleList.vue";
import UserList from "../../../js/components/permissions/userList.vue"
import RemovePermission from "../../../js/components/permissions/removePermission.vue"
import AddUserToRole from "../../../js/components/admin/addUserToRole.vue"
import Vue from "vue";

describe("roleList", () => {

    const mockRoles = [
        {
            name: "Funders",
            members: [
                {
                    email: "user1@example.com",
                    username: "user1",
                    display_name: "User One"
                }
            ],
            permissions: []
        },
        {
            name: "Science",
            members: [],
            permissions: []
        }
    ];

    const testPermission = {
        name: "test.perm",
        scope_id : "report",
        scope_prefix: "r1"
    };

    const mockAvailableUsers = ["user1@example.com", "user2@example.com", "user3@example.com"];

    it('renders roles with non-removable members', () => {

        const wrapper = shallowMount(RoleList,
            {
                propsData: {
                    roles: mockRoles,
                    showMembers: true,
                    canRemoveMembers: false,
                    permission: testPermission
                }
            });

        const userLists = wrapper.findAll(UserList);
        expect(userLists.length).toBe(1);

        expect(userLists.at(0).props().users).toEqual(expect.arrayContaining(mockRoles[0].members));
        expect(userLists.at(0).props().canRemove).toBe(false);
        expect(userLists.at(0).props().permission).toStrictEqual(testPermission);
    });

    it('renders roles with removable members', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canRemoveMembers: true
            }
        });

        const userLists = wrapper.findAll(UserList);

        expect(userLists.length).toBe(1);
        expect(userLists.at(0).props().canRemove).toBe(true);

    });

    it('renders roles with addable members', async () => {
        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canAddMembers: true,
                availableUsers: mockAvailableUsers
            }
        });

        //don't expect the add user control to appear unless a role is epanded
        expect(wrapper.findAll(AddUserToRole).length).toBe(0);

        wrapper.find('.expander').trigger("click");
        await Vue.nextTick();

        expect(wrapper.findAll(AddUserToRole).length).toBe(1);
        const addUser = wrapper.find(AddUserToRole);
        expect(addUser.props().role).toBe("Funders");
        //should have filtered to available users not already in role
        expect(addUser.props().availableUsers).toStrictEqual(["user2@example.com", "user3@example.com"]);
    });

    it('does not render addUserToRole is canAddMembers is false', async () => {
        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canAddMembers: false,
                availableUsers: mockAvailableUsers
            }
        });

        wrapper.find('.expander').trigger("click");
        await Vue.nextTick();

        expect(wrapper.findAll(AddUserToRole).length).toBe(0);
    });

    it('renders removable roles', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(RemovePermission).length).toBe(2);
    });

    it('renders non-removable roles', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canRemoveRoles: false,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(RemovePermission).length).toBe(0);
    });

    it('emits removed event when removePermission does', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        wrapper.findAll(RemovePermission).at(0).vm.$emit("removed");
        expect(wrapper.emitted().removed[0]).toStrictEqual(["role"]);
    });

    it('emits removed event when userList does', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                canRemoveMembers: true,
                canRemoveRoles: true
            }
        });

        wrapper.find(UserList).vm.$emit("removed", "bob");
        expect(wrapper.emitted().removed[0]).toStrictEqual(["bob", "Funders", undefined]);
    });

    it('emits added-user-to-role event when user is added', async () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                showMembers: true,
                availableUsers: mockAvailableUsers,
                canAddMembers: true
            }
        });

        //expand a role
        wrapper.find('.expander').trigger("click");
        await Vue.nextTick();

        const addUser = wrapper.find(AddUserToRole);
        addUser.vm.$emit("added", "user2@example.com");

        expect(wrapper.emitted()["added-user-to-role"][0]).toStrictEqual(["user2@example.com", "Funders"]);
    });

    it('can expand and collapse members', async () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                showMembers: true,
                roles: mockRoles
            }
        });

        const roles = wrapper.findAll("ul.roles > li");
        const roleWithMembers = roles.at(0);
        expect(roleWithMembers.classes("has-members")).toBe(true);

        const membersList = roleWithMembers.find(UserList);

        expect(membersList.isVisible()).toBe(false);
        expect(roleWithMembers.classes("open")).toBe(false);

        roleWithMembers.find('span').trigger("click");

        await Vue.nextTick();

        expect(membersList.isVisible()).toBe(true);
        expect(roleWithMembers.classes("open")).toBe(true);

        roleWithMembers.find('.expander').trigger("click");

        await Vue.nextTick();

        expect(membersList.isVisible()).toBe(false);
        expect(roleWithMembers.classes("open")).toBe(false);
    });

    it('does not render member list for roles with no members', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                showMembers: true,
                roles: mockRoles
            }
        });

        const roles = wrapper.findAll("ul.roles > li");
        const roleWithMembers = roles.at(1);
        expect(roleWithMembers.findAll(UserList).length).toBe(0);
        expect(roleWithMembers.classes("has-members")).toBe(false);
    });

});