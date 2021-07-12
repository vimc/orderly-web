import Vue from "vue";
import {mount} from "@vue/test-utils";
import ReportList from "../../../js/components/runReport/reportList.vue";

function getWrapper(selectedReport = "") {
    return mount(ReportList, {
        propsData: {
            reports: [
                {name: "report2", date: new Date(2021, 3, 21, 9, 10).toISOString()},
                {name: "report1", date: null}
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

    it("does not fire event on mount if report name is not valid", () => {
        const wrapper = getWrapper("report");
        expect(wrapper.emitted()["update:report"]).toBeFalsy();
    });

    it("typeahead filters list correctly", async (done) => {
        const wrapper = getWrapper();

        wrapper.find("input").setValue("rt2");

        await Vue.nextTick();

        let reportSuggestions = wrapper.findAll("a div.sr-only");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report2");

        let reportDates = wrapper.findAll("a span.text-muted");
        expect(reportDates.length).toBe(1);
        expect(reportDates.at(0).text()).toBe("Last run: Wed Apr 21 2021, 09:10");

        wrapper.find("input").setValue("rt1");

        await Vue.nextTick();

        reportSuggestions = wrapper.findAll("a div.sr-only");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report1");

        reportDates = wrapper.findAll("a span.text-muted");
        expect(reportDates.length).toBe(1);
        expect(reportDates.at(0).text()).toBe("Last run: never");

        done();
    });

    it("updates query when report prop changes", async () => {
        const wrapper = getWrapper();
        wrapper.find("input").setValue("rt2");

        await Vue.nextTick();
        expect(wrapper.vm.$data.query).toBe("rt2");

        await wrapper.setProps({report: ""});
        expect(wrapper.vm.$data.query).toBe("");
    });
});
