import {mount} from '@vue/test-utils';
import {mockAxios} from "../../mockAxios";

import RoleList from "../../../js/components/reports/permissions/roleList.vue";

describe("roleList", () => {

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/user-groups/report-readers/')
            .reply(200, {"data": mockRoles});
    });

    const mockRoles = [
        {
            name: "Funders",
            members: [
                {
                    email: "user1@example.com",
                    username: "user1",
                    display_name: "User One",
                    can_remove: false
                }
            ]
        }
    ];

    it('renders roles with members', () => {

        const wrapper = mount(RoleList, {propsData: {
            roles: mockRoles
        }});

        const roles = wrapper.findAll("ul.roles > li");
        expect(roles.length).toBe(1);

        const roleWithMembers = roles.at(0);
        expect(roleWithMembers.classes("open")).toBe(false);
        expect(roleWithMembers.find(".role-name").text()).toBe("Funders");
    });

    it('can expand and collapse members', () => {

        const wrapper = mount(RoleList, {propsData: {
                roles: mockRoles
            }});

        const roles = wrapper.findAll("ul.roles > li");
        const roleWithMembers = roles.at(0);

        const membersList = roleWithMembers.find("ul");

        expect(membersList.isVisible()).toBe(false);
        expect(roleWithMembers.classes("open")).toBe(false);

        roleWithMembers.trigger("click");

        expect(membersList.isVisible()).toBe(true);
        expect(roleWithMembers.classes("open")).toBe(true);

        const members = membersList.findAll("li");
        expect(members.length).toBe(1);
        expect(members.at(0).find(".reader-display-name").text()).toBe("User One");
        expect(members.at(0).find(".text-muted.small.email").text()).toBe("user1@example.com");

        roleWithMembers.trigger("click");

        expect(membersList.isVisible()).toBe(false);
        expect(roleWithMembers.classes("open")).toBe(false);
    });

});