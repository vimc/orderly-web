import {shallowMount} from "@vue/test-utils";
import ReportLog from "../../../../js/components/vuex/reportLog/reportLog.vue";

describe("ReportLog", () => {

    it("renders ReportLog", async () => {
        const wrapper = shallowMount(ReportLog);
        expect(wrapper.find("h2").text()).toBe("Running report logs");
    });

});
