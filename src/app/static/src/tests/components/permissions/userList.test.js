import User from "../../../js/components/permissions/user.vue"
import UserList from "../../../js/components/permissions/userList.vue"
import {shallowMount} from "@vue/test-utils";

describe("userList", () => {

    const users = [
        {
            email: "test.user@example.com",
            username: "test.user",
            display_name: "Test User"
        },
        {
            email: "another.user@example.com",
            username: "another.user",
            display_name: "Another User"
        }
    ];

    it("displays removable users", () => {

        const wrapper = shallowMount(UserList, {
            propsData: {
                users: users,
                canRemove: true
            }
        });

        expect(wrapper.classes()).toContain("removable-users-list");

        const firstUserProps = wrapper.findAll(User).at(0).props();

        expect(firstUserProps.displayName).toBe("Test User");
        expect(firstUserProps.email).toBe("test.user@example.com");
        expect(firstUserProps.canRemove).toBe(true);

        const secondUserProps = wrapper.findAll(User).at(1).props();

        expect(secondUserProps.displayName).toBe("Another User");
        expect(secondUserProps.email).toBe("another.user@example.com");
        expect(secondUserProps.canRemove).toBe(true);
    });

    it("displays extra css classes", () => {

        const wrapper = shallowMount(UserList, {
            propsData: {
                users: users,
                canRemove: true,
                cssClass: "test-class"
            }
        });

        expect(wrapper.classes()).toContain("removable-users-list");
        expect(wrapper.classes()).toContain("list-unstyled");
        expect(wrapper.classes()).toContain("test-class");
    });

    it("displays non-removable users", () => {

        const wrapper = shallowMount(UserList, {
            propsData: {
                users: users,
                canRemove: false
            }
        });

        expect(wrapper.classes()).toContain("removable-users-list");

        const firstUserProps = wrapper.findAll(User).at(0).props();

        expect(firstUserProps.displayName).toBe("Test User");
        expect(firstUserProps.email).toBe("test.user@example.com");
        expect(firstUserProps.canRemove).toBe(false);

        const secondUserProps = wrapper.findAll(User).at(1).props();

        expect(secondUserProps.displayName).toBe("Another User");
        expect(secondUserProps.email).toBe("another.user@example.com");
        expect(secondUserProps.canRemove).toBe(false);

    });

    it("emits removed event when child component does", () => {

        const wrapper = shallowMount(UserList, {
            propsData: {
                users: users,
                canRemove: true
            }
        });

        wrapper.findAll(User).at(0).vm.$emit("removed");
        expect(wrapper.emitted().removed[0]).toStrictEqual(["user"])
    });

});