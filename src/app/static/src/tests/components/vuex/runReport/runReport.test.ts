import {shallowMount} from "@vue/test-utils";
import RunReport from "../../../../js/components/vuex/runReport/runReport.vue";

describe("RunReport", () => {

    it("renders RunReport", async () => {
        const wrapper = shallowMount(RunReport);
        expect(wrapper.find("h2").text()).toBe("Run a report");
    });

});
