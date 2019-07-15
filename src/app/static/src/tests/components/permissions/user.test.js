import User from "../../../js/components/permissions/user.vue"
import RemovePermission from "../../../js/components/permissions/removePermission.vue"
import {shallowMount} from "@vue/test-utils";

describe("user", () => {

    it("displays removable user", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        expect(wrapper.classes()).toContain("removable-user");
        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll(RemovePermission).length).toBe(1);
    });

    it("displays non-removable report reader", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: false
            }
        });

        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll(RemovePermission).length).toBe(0);
    });

    it("emits removed event", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        wrapper.find(RemovePermission).vm.$emit("removed");
        expect(wrapper.emitted().removed).toBeDefined();
    })
});