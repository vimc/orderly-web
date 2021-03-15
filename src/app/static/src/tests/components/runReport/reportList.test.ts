import Vue from "vue";
import {mount} from "@vue/test-utils";
import ReportList from "../../../js/components/runReport/reportList.vue";

function getWrapper(selectedReport = "") {
    return mount(ReportList, {
        propsData: {
            reports: [
                {name: "report2", date: null},
                {name: "report1", date: new Date().toISOString()}
            ],
            initialSelectedReport: selectedReport
        }
    });
}

describe("reportList", () => {

    it("renders typeahead correctly and fires event on selection", (done) => {
        const wrapper = getWrapper();

        const reportSuggestions = wrapper.findAll("a div.sr-only");
        expect(reportSuggestions.length).toBe(2);
        expect(reportSuggestions.at(0).text()).toBe("report1");
        expect(reportSuggestions.at(1).text()).toBe("report2");
        reportSuggestions.at(1).trigger("click");
        expect(wrapper.emitted()["update:report"].length).toBe(1);
        expect(wrapper.emitted()["update:report"][0]).toEqual(["report2"]);

        done();
    });

    it("renders correctly and fires event on mount", () => {
        const wrapper = getWrapper("report2");
        expect(wrapper.emitted()["update:report"].length).toBe(1);
        expect(wrapper.emitted()["update:report"][0]).toEqual(["report2"]);
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
