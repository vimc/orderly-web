import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import PermissionList from "../../../js/components/admin/permissionList.vue";
import AddPermission from "../../../js/components/admin/addPermission.vue";

describe("permission list", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    it("displays scope if there is a specific scope", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [{
                    name: "reports.read",
                    scope_id: "r1",
                    scope_prefix: "report"
                }],
                allPermissions: []
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
                }],
                allPermissions: []
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
                permissions: [perm],
                allPermissions: ["reports.read"]
            }
        });

        rendered.find(".remove").trigger("click");

        await Vue.nextTick();

        expect(rendered.emitted().removed[0][0]).toStrictEqual(perm);
    });

    it("available permissions are any global permissions that the user doesn't have", () => {

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
                }],
                allPermissions: ["reports.read", "reports.review", "users.manage"]
            }
        });

        expect(rendered.find(AddPermission).props().availablePermissions)
            .toStrictEqual(["reports.read", "users.manage"])
    });

    it("only shows list of permissions if there are any", () => {
        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [],
                allPermissions: ["reports.read"]
            }
        });

        expect(rendered.findAll("ul").length).toBe(0);
    });


});
