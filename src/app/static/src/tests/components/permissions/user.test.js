import User from "../../../js/components/permissions/user.vue"
import {mount} from "@vue/test-utils";

describe("user", () => {

    it("displays removable user", () => {

        const wrapper = mount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        expect(wrapper.classes()).toContain("removable-user");
        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll('span.remove-user').length).toBe(1);
    });

    it("displays non-removable report reader", () => {

        const wrapper = mount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: false
            }
        });

        expect(wrapper.find('span.display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll('span.remove-user').length).toBe(0);
    });

    it("emits remove event", () => {

        const wrapper = mount(User, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        wrapper.find('span.remove-user').trigger("click");
        expect(wrapper.emitted().remove[0])
            .toEqual(expect.arrayContaining(["test.user@example.com"]))
    })
});