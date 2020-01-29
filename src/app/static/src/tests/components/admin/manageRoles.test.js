import {mount} from '@vue/test-utils';
import ManageRoles from "../../../js/components/admin/manageRoles.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/permissions/roleList.vue"
import AddRole from "../../../js/components/admin/addRole.vue";
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

    it('renders addRole', () => {
        const wrapper = mount(ManageRoles);
        expect(wrapper.find(AddRole).props().error).toBe("");
        expect(wrapper.find(AddRole).props().defaultMessage).toBe("");
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

    it('refreshes roles when role list emits removed event', (done) => {
        const wrapper = mount(ManageRoles);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            wrapper.find(RoleList).vm.$emit("removed");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe("http://app/roles/");
                done();
            });
        });
    });

    it('refreshes roles when role list emits added event', (done) => {
        const wrapper = mount(ManageRoles);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            wrapper.find(RoleList).vm.$emit("added");

            setTimeout(() => {
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe("http://app/roles/");
                done();
            });
        });
    });

    it('adds role when addRole emits added event', (done) => {
        mockAxios.onPost('http://app/user-groups/')
            .reply(200);

        const wrapper = mount(ManageRoles);
        setTimeout(() => {
            wrapper.find(AddRole).vm.$emit("added", "NewRole");
            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.post[0].url).toBe("http://app/user-groups/");
                expect(mockAxios.history.post[0].method).toBe('post');
                const data = JSON.parse(mockAxios.history.post[0].data);
                expect(data.name).toBe("NewRole");

                //should have refreshed roles too
                expect(mockAxios.history.get.length).toBe(3);
                expect(mockAxios.history.get[2].url).toBe("http://app/roles/");

                expect(wrapper.vm.$data.addRoleError).toBe("");
                expect(wrapper.vm.$data.addRoleDefaultMessage).toBe("");

                done();
            });
        });
    });

    it('sets errors when add role fails', (done) => {
        mockAxios.onPost('http://app/user-groups/')
            .reply(500, "TEST ERROR");

        const wrapper = mount(ManageRoles);
        setTimeout(() => {
            wrapper.find(AddRole).vm.$emit("added", "NewRole");
            setTimeout(() => {
                //should not have refreshed roles
                expect(mockAxios.history.get.length).toBe(2);

                expect(wrapper.vm.$data.addRoleError.response.data).toBe("TEST ERROR");
                expect(wrapper.vm.$data.addRoleDefaultMessage).toBe("could not add role 'NewRole'");

                done();
            });
        });
    });
});