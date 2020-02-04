import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import PermissionList from "../../../js/components/admin/permissionList.vue";
import AddPermission from "../../../js/components/admin/addPermission.vue";

describe("permission list", () => {

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet("http://app/typeahead/permissions/")
            .reply(200, {
                "data": ["reports.read", "reports.review", "users.manage"]
            })
    });

    it("fetches permissions for typeahead on mount", (done) => {
        const rendered = shallowMount(PermissionList,
            {
                propsData: {
                    permissions: []
                }
            });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(rendered.vm.$data.allPermissions).toStrictEqual(["reports.read", "reports.review", "users.manage"]);
            done();
        });
    });

    it("displays scope if there is a specific scope", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [{
                    name: "reports.read",
                    scope_id: "r1",
                    scope_prefix: "report"
                }]
            }
        });

        expect(rendered.find("li .name").text()).toBe("reports.read / report:r1");
    });

    it("does not display scope if global", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [{
                    name: "reports.read",
                    scope_id: "",
                    scope_prefix: null
                }]
            }
        });

        expect(rendered.find("li .name").text()).toBe("reports.read");
    });

    it("emits removed event when remove is clicked", async () => {
        const perm = {
            name: "reports.read",
            scope_id: "",
            scope_prefix: null
        };

        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [perm]
            }
        });

        rendered.find(".remove").trigger("click");

        await Vue.nextTick();

        expect(rendered.emitted().removed[0][0]).toStrictEqual(perm);
    });

    it("available permissions are any global permissions that the user doesn't have", async () => {

        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [{
                    name: "reports.review",
                    scope_id: "",
                    scope_prefix: null
                }, {
                    name: "reports.read",
                    scope_id: "r1",
                    scope_prefix: "report"
                }]
            }
        });

        rendered.setData({
            allPermissions: ["reports.read", "reports.review", "users.manage"]
        });

        await Vue.nextTick();

        expect(rendered.find(AddPermission).props().availablePermissions)
            .toStrictEqual(["reports.read", "users.manage"])
    });

    it("only shows list of permissions if there are any", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: []
            }
        });

        expect(rendered.findAll("ul").length).toBe(0);
    });

    it("permissions are sorted", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [
                    {
                        name: "reports.read",
                        scope_id: "r2",
                        scope_prefix: "report"
                    },
                    {
                        name: "reports.read",
                        scope_id: "",
                        scope_prefix: null
                    }, {
                        name: "reports.review",
                        scope_id: "",
                        scope_prefix: null
                    }, {
                        name: "reports.read",
                        scope_id: "r1",
                        scope_prefix: "report"
                    }]
            }
        });

        const renderedPermissionNames = rendered.findAll(".name");
        expect(renderedPermissionNames.at(0).text()).toBe("reports.read");
        expect(renderedPermissionNames.at(1).text()).toBe("reports.read / report:r1");
        expect(renderedPermissionNames.at(2).text()).toBe("reports.read / report:r2");
        expect(renderedPermissionNames.at(3).text()).toBe("reports.review");
    });

    it("renders direct permissions", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                userGroup: "test",
                permissions: [
                    {
                        name: "reports.read",
                        scope_id: "",
                        scope_prefix: null,
                        source: "test"
                    }]
            }
        });

        expect(rendered.find(".name").classes()).not.toContain("text-muted");
        expect(rendered.findAll(".remove").length).toBe(1);
        expect(rendered.findAll("span").length).toBe(2);
        expect(rendered.findAll("span").at(1).text()).toBe("×");
    });

    it("renders in-direct permissions", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                userGroup: "test",
                permissions: [
                    {
                        name: "reports.read",
                        scope_id: "",
                        scope_prefix: null,
                        source: "something else"
                    }]
            }
        });

        expect(rendered.find(".name").classes()).toContain("text-muted");
        expect(rendered.findAll(".remove").length).toBe(0);
        expect(rendered.findAll("span").length).toBe(2);
        expect(rendered.findAll("span").at(1).classes()).toContain("text-muted");
        expect(rendered.findAll("span").at(1).text()).toBe("(something else)");
    });
});
