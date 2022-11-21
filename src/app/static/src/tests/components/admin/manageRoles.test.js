import {mount, shallowMount} from '@vue/test-utils';
import ManageRoles from "../../../js/components/admin/manageRoles.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/permissions/roleList.vue"
import AddRole from "../../../js/components/admin/addRole.vue";
import ErrorInfo from "../../../js/components/errorInfo";
import Vue from "vue";

describe("manageRoles", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/typeahead/emails/')
            .reply(200, {"data": mockEmails});
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
        },
        {
            name: "Science",
            members: []
        }
    ];

    const mockEmails = ["user1@example.com", "user2@example.com"];

    it('renders role list', async () => {
        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });

        await Vue.nextTick();

        expect(wrapper.findComponent(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.findComponent(RoleList).props().canRemoveRoles).toBe(true);
        expect(wrapper.findComponent(RoleList).props().canRemoveMembers).toBe(true);
        expect(wrapper.findComponent(RoleList).props().canAddMembers).toBe(true);

    });

    it('renders addRole', () => {
        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        expect(wrapper.findAllComponents(AddRole).length).toBe(1);
    });

    it('renders errorInfo', async () => {
        const wrapper = shallowMount(ManageRoles);
        wrapper.setData({
            error: "test-error",
            defaultMessage: "test-default"
        });

        await Vue.nextTick();

        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBe("test-error");
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("test-default");
    });

    it(`shows modal if showModal is true`, async () => {
        const wrapper = shallowMount(ManageRoles, {
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
        const wrapper = shallowMount(ManageRoles, {
            propsData: {
                roles: mockRoles,
                canDeleteRoles: true
            }
        });

        //showModal defaults to false
        expect(wrapper.find('#delete-role-confirm').classes('modal-hide')).toBe(true);
        expect(wrapper.find('#delete-role-confirm').classes('modal-show')).toBe(false);
    });

    it('fetches typeahead emails on mount', (done) => {
        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.findComponent(RoleList).props().availableUsers).toEqual(expect.arrayContaining(mockEmails));
            done();
        });
    });

    it('emits changed event when role list emits removedMember event', () => {
        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        wrapper.findComponent(RoleList).vm.$emit("removedMember");
        expect(wrapper.emitted().changed.length).toBe(1);
    });

    it('shows modal when role list emits removed event', async () => {
        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        wrapper.findComponent(RoleList).vm.$emit("removed", "Funders");

        await Vue.nextTick();

        expect(wrapper.vm.$data.showModal).toBe(true);
        expect(wrapper.vm.$data.roleToDelete).toBe("Funders");
        expect(wrapper.find('#delete-role-confirm').classes('modal-show')).toBe(true);
        expect(wrapper.find('#delete-role-confirm').text()).toContain('Are you sure you want to delete Funders role?')
    });

    it('emits changed event when role list emits added event', () => {
        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        wrapper.findComponent(RoleList).vm.$emit("added");
        expect(wrapper.emitted().changed.length).toBe(1);
    });


    it('cancelling delete role hides model and does not delete role', async () => {
        const wrapper = shallowMount(ManageRoles, {
            propsData: {
                roles: mockRoles
            }
        });
        wrapper.setData({
            showModal: true,
            roleToDelete: "Funders"
        });
        await Vue.nextTick();

        await wrapper.find('#cancel-delete-btn').trigger('click');

        await Vue.nextTick();
        await Vue.nextTick();
        expect(wrapper.vm.$data.showModal).toBe(false);
        expect(wrapper.vm.$data.roleToDelete).toBe("");
        expect(mockAxios.history.delete.length).toBe(0);
        expect(wrapper.emitted().deleted).toBe(undefined);


    });

    it('confirming delete role hides model and deletes role', async () => {
        const url = 'http://app/roles/Funders/';
        mockAxios.onDelete(url)
            .reply(200);

        const wrapper = shallowMount(ManageRoles, {
            propsData: {
                roles: mockRoles
            }
        });
        wrapper.setData({
            showModal: true,
            roleToDelete: "Funders"
        });
        await Vue.nextTick();

        await wrapper.find('#confirm-delete-btn').trigger('click');

        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.vm.$data.showModal).toBe(false);
        expect(wrapper.vm.$data.roleToDelete).toBe("");
        expect(mockAxios.history.delete.length).toBe(1);
        expect(mockAxios.history.delete[0].url).toBe(url);
        expect(wrapper.emitted().changed.length).toBe(1);
    });

    it('sets error if deleting role fails', async () => {
        const url = 'http://app/roles/Funders/';
        mockAxios.onDelete(url)
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(ManageRoles, {
            propsData: {
                roles: mockRoles
            }
        });
        wrapper.setData({
            showModal: true,
            roleToDelete: "Funders"
        });
        await Vue.nextTick();

        await wrapper.find('#confirm-delete-btn').trigger('click');

        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.vm.$data.showModal).toBe(false);
        expect(wrapper.vm.$data.roleToDelete).toBe("");
        expect(wrapper.emitted().deleted).toBe(undefined);
        expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
    });

    it('adds role when addRole emits added event', (done) => {
        mockAxios.onPost('http://app/roles/')
            .reply(200);

        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        setTimeout(() => {
            wrapper.findComponent(AddRole).vm.$emit("added", "NewRole");
            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.post[0].url).toBe("http://app/roles/");
                expect(mockAxios.history.post[0].method).toBe('post');
                const data = JSON.parse(mockAxios.history.post[0].data);
                expect(data.name).toBe("NewRole");

                //should have refreshed roles too
                expect(wrapper.emitted().changed.length).toBe(1);

                expect(wrapper.vm.$data.error).toBe("");
                expect(wrapper.vm.$data.defaultMessage).toBe("");

                done();
            });
        });
    });

    it('sets errors when add role fails', (done) => {
        mockAxios.onPost('http://app/roles/')
            .reply(500, "TEST ERROR");

        const wrapper = mount(ManageRoles, {
            propsData: {roles: mockRoles}
        });
        setTimeout(() => {
            wrapper.findComponent(AddRole).vm.$emit("added", "NewRole");
            setTimeout(() => {
                expect(wrapper.emitted().changed).toBeUndefined();

                expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
                expect(wrapper.vm.$data.defaultMessage).toBe("could not add role 'NewRole'");

                done();
            });
        });
    });
});