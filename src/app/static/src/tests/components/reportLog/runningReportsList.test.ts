import Vue from "vue";
import {mount} from "@vue/test-utils";
import RunningReportsList from "../../../js/components/reportLog/runningReportsList.vue";

function getWrapper() {
    return mount(RunningReportsList, {
        propsData: {
            reports: [
                {name: "report2", date: new Date().toISOString(), key: "report2Key"},
                {name: "report1", date: new Date().toISOString(), key: "report1Key"}
            ]
        }
    });
}

describe("runningReportsList", () => {

    it("renders typeahead correctly and fires event on selection", (done) => {
        const wrapper = getWrapper();

        const reportSuggestions = wrapper.findAll("a div.sr-only");
        expect(reportSuggestions.length).toBe(2);
        expect(reportSuggestions.at(0).text()).toBe("report1");
        expect(reportSuggestions.at(1).text()).toBe("report2");
        reportSuggestions.at(1).trigger("click");
        expect(wrapper.emitted()["update:key"].length).toBe(1);
        expect(wrapper.emitted()["update:key"][0]).toEqual(["report2Key"]);

        done();
    });

    it("typeahead filters list correctly", async (done) => {
        const wrapper = getWrapper();

        wrapper.find("input").setValue("rt2");

        await Vue.nextTick();

        const reportSuggestions = wrapper.findAll("a div.sr-only");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report2");

        done();
    });

});
