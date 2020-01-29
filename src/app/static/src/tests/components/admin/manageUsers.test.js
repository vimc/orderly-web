import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import ManageUsers from "../../../js/components/admin/manageUsers.vue";

describe("manage users", () => {

    const mockUsers =  [
        {
            username: "a.user",
            display_name: "Some name",
            email: "a@example.com"
        },
        {
            username: "b.user",
            display_name: "Some other name",
            email: "b@example.com"
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
        expect(rendered.find("li").text()).toBe("Some name");

        rendered.find("input").setValue("A.");
        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(1);
        expect(rendered.find("li").text()).toBe("Some name");
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
        expect(rendered.find("li").text()).toBe("Some other name");

        rendered.find("input").setValue("Other");
        await Vue.nextTick();
        expect(rendered.findAll("li").length).toBe(1);
        expect(rendered.find("li").text()).toBe("Some other name");
    });


});
