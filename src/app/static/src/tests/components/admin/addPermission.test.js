import Vue from "vue";
import {mount} from "@vue/test-utils";
import AddPermission from "../../../js/components/admin/addPermission.vue";

describe("add permission", () => {

    it("shows error if invalid permission is selected", async () => {
        const wrapper = mount(AddPermission, {
            propsData: {
                email: "test@test.com",
                availablePermissions: ["reports.read"]
            }
        });

        wrapper.find("input").setValue("reports.review");

        await Vue.nextTick();

        wrapper.find("button").trigger("click");

        await Vue.nextTick();

        expect(wrapper.find(".text-danger").text())
            .toBe("Error: reports.review is not an available permission or already belongs to test@test.com");

    });

});
