import {shallowMount} from '@vue/test-utils';
import RoleList from "../../../js/components/permissions/roleList.vue";
import UserList from "../../../js/components/permissions/userList.vue"
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

    it('renders removable roles', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(".remove-user-group").length).toBe(2);
    });

    it('renders non-removable roles', () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: false,
                canRemoveMembers: true
            }
        });

        expect(wrapper.findAll(".remove-user-group").length).toBe(0);
    });

    it('removes role and emits removed event', (done) => {

        mockAxios.onPost('http://app/user-groups/Funders/actions/associate-permission/')
            .reply(200);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        wrapper.findAll(".remove-user-group").at(0).trigger("click");

        setTimeout(() => {
            expect(wrapper.emitted().removed[0]).toStrictEqual(["Funders"]);
            done();
        })
    });

    it('sets error if removing role fails', (done) => {

        mockAxios.onPost('http://app/user-groups/Funders/actions/associate-permission/')
            .reply(500);

        const wrapper = shallowMount(RoleList, {
            propsData: {
                roles: mockRoles,
                canRemoveRoles: true,
                canRemoveMembers: true
            }
        });

        wrapper.findAll(".remove-user-group").at(0).trigger("click");

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError")).toBeDefined();
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("could not remove Funders");
            done();
        });

    });

    it('removes member and emits removed event', (done) => {

        mockAxios.onDelete('http://app/user-groups/Funders/user/bob')
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

        mockAxios.onPost('http://app/user-groups/Funders/user/bob')
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

    it('can expand and collapse members', async () => {

        const wrapper = shallowMount(RoleList, {
            propsData: {
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
                roles: mockRoles
            }
        });

        const roles = wrapper.findAll("ul.roles > li");
        const roleWithMembers = roles.at(1);
        expect(roleWithMembers.findAll(UserList).length).toBe(0);
        expect(roleWithMembers.classes("has-members")).toBe(false);
    });

});