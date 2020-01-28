import {shallowMount} from "@vue/test-utils";
import PermissionList from "../../../js/components/admin/permissionList.vue";

describe("permission list", () => {

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

        expect(rendered.find("li").text()).toBe("reports.read / report:r1");
    })
});
