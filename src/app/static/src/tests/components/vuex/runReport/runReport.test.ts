import {shallowMount} from "@vue/test-utils";
import RunReport from "../../../../js/components/vuex/runReport/runReport.vue";
import GitSelections from "../../../../js/components/vuex/runReport/gitSelections.vue";

describe("RunReport", () => {

    it("renders header", async () => {
        const wrapper = shallowMount(RunReport);
        expect(wrapper.find("h2").text()).toBe("Run a report");
    });

    it("renders GitSelections", async () => {
        const wrapper = shallowMount(RunReport);
        expect(wrapper.findComponent(GitSelections).exists()).toBe(true);
    });

});
