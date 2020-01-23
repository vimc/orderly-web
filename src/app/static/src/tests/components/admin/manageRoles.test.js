import {mount} from '@vue/test-utils';
import ManageRoles from "../../../js/components/admin/manageRoles.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/permissions/roleList.vue"
import Vue from "vue";

describe("manageRoles", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/roles/')
            .reply(200, {"data": mockRoles});
        mockAxios.onGet('http://app/typeahead/emails/')
            .reply(200, {"data": mockEmails});
    });

    const mockRoles =  [
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
        },
        {
            name: "Science",
            members: []
        }
    ];

    const mockEmails = ["user1@example.com", "user2@example.com"];

    it('renders role list', async () => {
        const wrapper = mount(ManageRoles);
        wrapper.setData({
            roles: mockRoles
        });

        await Vue.nextTick();

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemoveRoles).toBe(false);
        expect(wrapper.find(RoleList).props().canRemoveMembers).toBe(true);
        expect(wrapper.find(RoleList).props().canAddMembers).toBe(true);

    });

    it('fetches roles and typeahead emails on mount', (done) => {
        const wrapper = mount(ManageRoles);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            expect(wrapper.find(RoleList).props().availableUsers).toEqual(expect.arrayContaining(mockEmails));
            done();
        });
    });

    it('removes role member when role list emits removed event', () => {
        const roles =  [
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
            },
            {
                name: "Science",
                members: []
            }
        ];

        const wrapper = mount(ManageRoles);
        wrapper.setData({
            roles: roles
        });

        wrapper.find(RoleList).vm.$emit("removed", "user1@example.com", "Funders");

        expect(roles[0].members.length).toBe(0);
    });

    it('refreshes roles when role list emits added event', async (done) => {
        const wrapper = mount(ManageRoles);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            wrapper.find(RoleList).vm.$emit("added-user-to-role");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe("http://app/roles/");
                done();
            });
        });
    });
});