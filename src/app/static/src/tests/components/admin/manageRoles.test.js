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

    it('renders role list', async () => {
        const wrapper = mount(ManageRoles);
        wrapper.setData({
            roles: mockRoles
        });

        await Vue.nextTick();

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemoveRoles).toBe(false);
        expect(wrapper.find(RoleList).props().canRemoveMembers).toBe(true);

    });

    it('fetches roles on mount', (done) => {
        const wrapper = mount(ManageRoles);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
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
});