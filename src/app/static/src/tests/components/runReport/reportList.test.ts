import Vue from "vue";
import {mount} from "@vue/test-utils";
import ReportList from "../../../js/components/runReport/reportList.vue";
import VueSelect from "vue-select";

const report1 = {name: "report1", date: null};
const report2 = {name: "report2", date: new Date(2021, 3, 21, 9, 10).toISOString()};

function getWrapper(selectedReport = null) {
    return mount(ReportList, {
        propsData: {
            reports: [report2, report1],
            selectedReport: selectedReport
        }
    });
}

describe("reportList", () => {

    it("renders typeahead correctly and fires event on selection", async () => {
        const wrapper = getWrapper();

        await Vue.nextTick();

        (wrapper.findComponent(VueSelect).vm.$refs.search as any).focus();

        await Vue.nextTick();

        const reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(2);
        expect(reportSuggestions.at(0).text()).toBe("report1 Last run: never");
        expect(reportSuggestions.at(1).text()).toBe("report2 Last run: Wed Apr 21 2021, 09:10");

        (wrapper.findComponent(VueSelect).vm as any).select(report2);
        await Vue.nextTick();

        expect(wrapper.emitted("update:selectedReport").length).toBe(1);
        expect(wrapper.emitted("update:selectedReport")[0][0]).toBe(report2);
    });

    it("typeahead filters list correctly", async () => {
        const wrapper = getWrapper();

        (wrapper.findComponent(VueSelect).vm.$refs.search as any).focus()

        await wrapper.find("input").setValue("rt2");
        await Vue.nextTick();
        let reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report2 Last run: Wed Apr 21 2021, 09:10");

        await wrapper.find("input").setValue("rt1");
        reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toBe("report1 Last run: never");
    });

});
