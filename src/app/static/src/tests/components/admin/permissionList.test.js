import Vue from "vue";
import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import PermissionList from "../../../js/components/admin/permissionList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";

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


});
