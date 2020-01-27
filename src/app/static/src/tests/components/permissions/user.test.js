import User from "../../../js/components/permissions/user.vue"
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
        expect(wrapper.findAll(".remove-user-group").length).toBe(1);
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
        expect(wrapper.findAll(".remove-user-group").length).toBe(0);
    });

    it("emits removed event", () => {

        const wrapper = shallowMount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        wrapper.find(".remove-user-group").trigger("click");
        expect(wrapper.emitted().removed).toBeDefined();
    });

});