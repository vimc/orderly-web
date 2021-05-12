import Vue from "vue";
import {mount} from "@vue/test-utils";
import RunningReportsList from "../../../js/components/reportLog/runningReportsList.vue";

function getWrapper(key: string = '') {
    return mount(RunningReportsList, {
        propsData: {
            initialSelectedKey: key,
            reports: [
                {name: "report2", date: new Date(2021, 3, 21, 9, 10).toISOString(), key: "report2Key"},
                {name: "report1", date: new Date(2021, 3, 21, 10, 1).toISOString(), key: "report1Key"}
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

        const reportDates = wrapper.findAll("a span.text-muted");
        expect(reportDates.length).toBe(1);
        expect(reportDates.at(0).text()).toBe("Run started: Wed Apr 21 2021, 09:10");

        done();
    });

    it("typeahead comes with correct report name preselected", async (done) => {
        const wrapper = getWrapper("report1Key");

        await Vue.nextTick();
        expect(wrapper.vm.$data.query).toBe("report1")
        const reportSuggestions = wrapper.findAll("a div.sr-only");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report1");

        done();
    });

});
