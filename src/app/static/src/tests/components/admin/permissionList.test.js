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

    it("can remove permission", async (done) => {
        mockAxios.onPost('http://app/user-groups/test/actions/associate-permission/')
            .reply(200);

        const perm = {
            name: "reports.read",
            scope_id: "",
            scope_prefix: null
        };

        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [perm],
                email: "test"
            }
        });

        rendered.find(".remove").trigger("click");

        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);

        setTimeout(() => {
            expect(rendered.emitted().removed[0][0]).toBe(perm);
            expect(rendered.find(ErrorInfo).props("apiError")).toBe(null);
            done();
        });
    });

    it("sets error if removing permission fails", async (done) => {
        mockAxios.onPost('http://app/user-groups/test/actions/associate-permission/')
            .reply(500);

        const perm = {
            name: "reports.read",
            scope_id: "",
            scope_prefix: null
        };

        const rendered = shallowMount(PermissionList, {
            propsData: {
                permissions: [perm],
                email: "test"
            }
        });

        rendered.find(".remove").trigger("click");

        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);

        setTimeout(() => {
            expect(rendered.emitted().removed).toBeUndefined();
            expect(rendered.find(ErrorInfo).props("defaultMessage")).toBe("could not remove reports.read from test");
            expect(rendered.find(ErrorInfo).props("apiError")).not.toBe(null);
            done();
        });
    });

});
