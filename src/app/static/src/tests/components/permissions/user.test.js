import User from "../../../js/components/permissions/user.vue"
import RemovePermission from "../../../js/components/permissions/removePermission.vue"
import RemoveRole from "../../../js/components/permissions/removeRole.vue"
import {shallowMount} from "@vue/test-utils";

describe("user", () => {

    it("displays removable user for permission", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true,
                permission: "test-permission"
            }
        });

        expect(wrapper.classes()).toContain("removable-user");
        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll(RemovePermission).length).toBe(1);
        expect(wrapper.findAll(RemoveRole).length).toBe(0);
    });

    it("displays removable user for role", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true,
                role: "Funder"
            }
        });

        expect(wrapper.classes()).toContain("removable-user");
        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll(RemoveRole).length).toBe(1);
        expect(wrapper.findAll(RemovePermission).length).toBe(0);
    });

    it("displays non-removable report reader", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: false,
                permission: "test-permission"
            }
        });

        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll(RemovePermission).length).toBe(0);
    });

    it("emits removed event for permission", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true,
                permission: "test-permission"
            }
        });

        wrapper.find(RemovePermission).vm.$emit("removed");
        expect(wrapper.emitted().removed).toBeDefined();
    });

    it("emits removed event for role", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test Role",
                canRemove: true,
                role: "Funder"
            }
        });

        wrapper.find(RemoveRole).vm.$emit("removed");
        expect(wrapper.emitted().removed).toBeDefined();
    })
});