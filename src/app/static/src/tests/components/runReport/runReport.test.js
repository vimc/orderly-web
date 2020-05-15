import {shallowMount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";

describe("reportTags", () => {
    it("renders as expected", () => {
        const wrapper = shallowMount(RunReport);
        expect(wrapper.text()).toBe("Run report coming soon!")
    });
});