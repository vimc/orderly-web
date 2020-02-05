import {shallowMount} from '@vue/test-utils';
import RoleList from "../../../js/components/permissions/roleList.vue";
import UserList from "../../../js/components/permissions/userList.vue"
import AddUserToRole from "../../../js/components/admin/addUserToRole.vue"
import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import ErrorInfo from "../../../js/components/errorInfo";

describe("roleList", () => {


    beforeEach(() => {
        mockAxios.reset();
    });

    const mockRoles = [
        {
            name: "Funders",
            members: [
                {
                    email: "user1@example.com",
                    username: "user1",
                    display_name: "User One"
                }
            ]
        },
        {
            name: "Science",
            members: []
        }
    ];

    const testPermission = {
        name: "test.perm",
        scope_id: "report",
        scope_prefix: "r1"
    };

    const mockAvailableUsers = ["user1@example.com", "user2@example.com", "user3@example.com"];

    it('renders roles with non-removable members', () => {

        const wrapper = shallowMount(RoleList,
            {
                propsData: {
                    roles: mockRoles,
                    canRemoveMembers: false,
                    permission: testPermission
                }
            });

        const userLists = wrapper.findAll(UserList);
        expect(userLists.length).toBe(1);

        expect(userLists.at(0).props().users).toEqual(expect.arrayContaining(mockRoles[0].members));
        expect(userLists.at(0).props().canRemove).toBe(false);
    });

    it('renders roles with removable members', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
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
                canRemoveRoles: true,
                canDeleteRoles: false,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(".remove").length).toBe(2);
    });

    it('renders deletable roles', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: false,
                canDeleteRoles: true,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(".remove").length).toBe(2);
    });


    it('renders non-removable and non-deletable roles', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: false,
                canDeleteRoles: false,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(".remove").length).toBe(0);
    });

    it('removes role and emits removed event', (done) => {

        mockAxios.onDelete('http://app/roles/Funders/permissions/reports.read/?scopePrefix=report&scopeId=r1')
            .reply(200);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                permission: {
                    scope_id: "r1",
                    scope_prefix: "report",
                    name: "reports.read"
                },
                roles: mockRoles,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        wrapper.findAll(".remove").at(0).trigger("click");

        setTimeout(() => {
            expect(wrapper.emitted().removed[0]).toStrictEqual(["Funders"]);
            done();
        })
    });

    it('sets error if removing role fails', (done) => {

        mockAxios.onDelete('http://app/roles/Funders/permissions/reports.read/?&scopePrefix=report&scopeId=r1')
            .reply(500);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                permission: {
                    scope_id: "r1",
                    scope_prefix: "report",
                    name: "reports.read"
                },
                roles: mockRoles,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        wrapper.findAll(".remove").at(0).trigger("click");

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError")).toBeDefined();
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("could not remove Funders");
            done();
        });

    });

    it('removes member and emits removed event', (done) => {

        mockAxios.onDelete('http://app/roles/Funders/users/bob')
            .reply(200);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveMembers: true,
                canRemoveRoles: true
            }
        });

        wrapper.find(UserList).vm.$emit("removed", "bob");

        setTimeout(() => {
            expect(wrapper.emitted().removed[0]).toStrictEqual(["Funders", "bob"]);
            done();
        });
    });

    it('sets error if removing member fails', (done) => {

        mockAxios.onPost('http://app/roles/Funders/users/bob')
            .reply(500);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        wrapper.find(UserList).vm.$emit("removed", "bob");

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError")).toBeDefined();
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("could not remove bob from Funders");
            done();
        });

    });

    it('emits added event when user is added', async () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                availableUsers: mockAvailableUsers,
                canAddMembers: true
            }
        });

        //expand a role
        wrapper.find('.expander').trigger("click");
        await Vue.nextTick();

        const addUser = wrapper.find(AddUserToRole);
        addUser.vm.$emit("added");

        expect(wrapper.emitted()["added"].length).toBe(1);
    });

    it('clicking remove to delete role shows confirm delete modal', async () => {
        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });

        wrapper.find('.remove').trigger("click");
        await Vue.nextTick();

        expect(wrapper.vm.$data.showModal).toBe(true);
        expect(wrapper.vm.$data.roleToDelete).toBe("Funders");
        expect(wrapper.find('#delete-role-confirm').classes('modal-show')).toBe(true);
        expect(wrapper.find('#delete-role-confirm').text()).toContain('Are you sure you want to delete Funders role?')

    });

    it('cancelling delete role hides model and does not delete role', async (done) => {
        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });
        wrapper.setData({
            showModal: true,
            roleToDelete: "Funders"
        });
        await Vue.nextTick();

        wrapper.find('#cancel-delete-btn').trigger('click');
        setTimeout(() => {
            expect(wrapper.vm.$data.showModal).toBe(false);
            expect(wrapper.vm.$data.roleToDelete).toBe("");
            expect(mockAxios.history.delete.length).toBe(0);
            expect(wrapper.emitted().deleted).toBe(undefined);
            done();
        })

    });

    it('confirming delete role hides model and deletes role', async (done) => {
        const url = 'http://app/roles/Funders/';
        mockAxios.onDelete(url)
            .reply(200);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });
        wrapper.setData({
            showModal: true,
            roleToDelete: "Funders"
        });
        await Vue.nextTick();

        wrapper.find('#confirm-delete-btn').trigger('click');
        setTimeout(() => {
            expect(wrapper.vm.$data.showModal).toBe(false);
            expect(wrapper.vm.$data.roleToDelete).toBe("");
            expect(mockAxios.history.delete.length).toBe(1);
            expect(mockAxios.history.delete[0].url).toBe(url);
            expect(wrapper.emitted().deleted.length).toBe(1);
            done();
        })

    });

    it('sets error if deleting role fails', async (done) => {
        const url = 'http://app/roles/Funders/';
        mockAxios.onDelete(url)
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });
        wrapper.setData({
            showModal: true,
            roleToDelete: "Funders"
        });
        await Vue.nextTick();

        wrapper.find('#confirm-delete-btn').trigger('click');
        setTimeout(() => {
            expect(wrapper.vm.$data.showModal).toBe(false);
            expect(wrapper.vm.$data.roleToDelete).toBe("");
            expect(wrapper.emitted().deleted).toBe(undefined);
            expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
            done();
        })

    });

    it('can expand and collapse members', async () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles
            }
        });

        const roles = wrapper.findAll("ul.roles > li");
        const roleWithMembers = roles.at(0);
        expect(roleWithMembers.classes("has-children")).toBe(true);

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
                roles: mockRoles
            }
        });

        const roles = wrapper.findAll("ul.roles > li");
        const roleWithoutMembers = roles.at(1);
        expect(roleWithoutMembers.findAll(UserList).length).toBe(0);
        expect(roleWithoutMembers.classes("has-children")).toBe(false);
    });

    it('adds has-children class if canAddMembers is true', async () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles
            }
        });

        const roleWithoutMembers = wrapper.findAll("ul.roles > li").at(1);
        expect(roleWithoutMembers.classes("has-children")).toBe(false);

        wrapper.setProps({
            canAddMembers: true
        });

        await Vue.nextTick();
        expect(roleWithoutMembers.classes("has-children")).toBe(true);

    });

    it(`shows modal if showModal is true`, async () => {
        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });

        wrapper.setData({
            showModal: true
        });
        await Vue.nextTick();

        expect(wrapper.find('#delete-role-confirm').classes('modal-hide')).toBe(false);
        expect(wrapper.find('#delete-role-confirm').classes('modal-show')).toBe(true);
    });

    it(`hides modal if showModal is false`, () => {
        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });

        //showModal defaults to false
        expect(wrapper.find('#delete-role-confirm').classes('modal-hide')).toBe(true);
        expect(wrapper.find('#delete-role-confirm').classes('modal-show')).toBe(false);
    });

    it('canRemoveOrDeleteRole returns true if canRemove, or canDelete and role is not Admin', () => {
        let wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: true,
                canDeleteRoles: false
            }
        });
        expect(wrapper.vm.canRemoveOrDeleteRole("Funders")).toBe(true)

        wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: false,
                canDeleteRoles: true
            }
        });
        expect(wrapper.vm.canRemoveOrDeleteRole("Funders")).toBe(true)
    });

    it('canRemoveOrDeleteRole returns false if cannot remove or delete, or role is Admin', () => {
        let wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: false,
                canDeleteRoles: false
            }
        });
        expect(wrapper.vm.canRemoveOrDeleteRole("Funders")).toBe(false)

        wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: false,
                canDeleteRoles: true
            }
        });
        expect(wrapper.vm.canRemoveOrDeleteRole("Admin")).toBe(false)
    });



});