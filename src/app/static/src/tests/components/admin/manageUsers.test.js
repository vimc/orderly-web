import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import {shallowMount, mount} from "@vue/test-utils";
import ManageUsers from "../../../js/components/admin/manageUsers.vue";
import PermissionList from "../../../js/components/admin/permissionList.vue";

describe("manage users", () => {

    const mockUsers = [
        {
            username: "a.user",
            display_name: "Some name",
            email: "a@example.com",
            permissions: []
        },
        {
            username: "b.user",
            display_name: "Some other name",
            email: "b@example.com",
            permissions: [{
                name: "reports.read",
                scope_id: "",
                scope_prefix: null
            }]
        }
    ];

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/users/')
            .reply(200, {"data": mockUsers});
    });

    it("fetches users on mount", (done) => {
        shallowMount(ManageUsers);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            done();
        });
    });

    it("matches users by case insensitive username", async () => {
        const rendered = shallowMount(ManageUsers);
        rendered.setData({allUsers: mockUsers});
        rendered.find("input").setValue("a.");
        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(1);
        expect(rendered.find("li .role-name").text()).toBe("Some name");

        rendered.find("input").setValue("A.");
        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(1);
        expect(rendered.find("li .role-name").text()).toBe("Some name");
    });

    it("matches users by case insensitive email", async () => {
        const rendered = shallowMount(ManageUsers);
        rendered.setData({allUsers: mockUsers});
        rendered.find("input").setValue("example");

        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(2);

        rendered.find("input").setValue("EXAmple");
        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(2);
    });

    it("matches users by case insensitive display name", async () => {
        const rendered = shallowMount(ManageUsers);
        rendered.setData({allUsers: mockUsers});
        rendered.find("input").setValue("other");

        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(1);
        expect(rendered.find("li .role-name").text()).toBe("Some other name");

        rendered.find("input").setValue("Other");
        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(1);
        expect(rendered.find("li .role-name").text()).toBe("Some other name");
    });

    it("renders permission list iff user has permissions", async () => {
        const rendered = shallowMount(ManageUsers);
        rendered.setData({allUsers: mockUsers});
        rendered.find("input").setValue("example");

        await Vue.nextTick();

        expect(rendered.findAll("li").length).toBe(2);
        expect(rendered.findAll(PermissionList).length).toBe(1);
        expect(rendered.find(PermissionList).props().permissions).toBe(mockUsers[1].permissions);

        expect(rendered.findAll("li").at(0).classes("has-children")).toBe(false);
        expect(rendered.findAll("li").at(1).classes("has-children")).toBe(true);
    });

    it("can open and close permission list", async () => {
        const rendered = shallowMount(ManageUsers);
        rendered.setData({allUsers: mockUsers});
        rendered.find("input").setValue("other");

        await Vue.nextTick();

        expect(rendered.find("li").classes()).not.toContain("open");
        expect(rendered.find(PermissionList).isVisible()).toBe(false);
        rendered.find(".expander").trigger("click");

        await Vue.nextTick();

        expect(rendered.find("li").classes()).toContain("open");
        expect(rendered.find(PermissionList).isVisible()).toBe(true);
    });

    it("removes permission from visible user when removed event is emitted", async () => {
        const rendered = mount(ManageUsers);
        rendered.setData({allUsers: mockUsers});
        rendered.find("input").setValue("other");

        await Vue.nextTick();

        rendered.find(".expander").trigger("click");

        await Vue.nextTick();

        expect(rendered.findAll("ul.children li").length).toBe(1);

        rendered.find(PermissionList).vm.$emit("removed", mockUsers[1].permissions[0]);

        await Vue.nextTick();

        expect(rendered.findAll("ul.children li").length).toBe(0);
    });
});
