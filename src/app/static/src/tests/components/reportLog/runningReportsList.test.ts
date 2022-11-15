import Vue from "vue";
import {mount} from "@vue/test-utils";
import RunningReportsList from "../../../js/components/reportLog/runningReportsList.vue";
import VueSelect from "vue-select";

const reports = [
    {name: "report2", date: new Date(2021, 3, 21, 9, 10).toISOString(), key: "report2Key"},
    {name: "report1", date: new Date(2021, 3, 21, 10, 1).toISOString(), key: "report1Key"}
];

function getWrapper(key = "") {
    return mount(RunningReportsList, {
        propsData: {
            initialSelectedKey: key,
            reports
        }
    });
}

describe("runningReportsList", () => {

    it("renders typeahead correctly and fires event on selection", async () => {
        const wrapper = getWrapper();

        await wrapper.findComponent(VueSelect).setData({open: true});
        await Vue.nextTick();

        const reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(2);
        expect(reportSuggestions.at(0).text()).toBe("report1 Run started: Wed Apr 21 2021, 10:01");
        expect(reportSuggestions.at(1).text()).toBe("report2 Run started: Wed Apr 21 2021, 09:10");

        (wrapper.findComponent(VueSelect).vm as any).select(reports[0]);
        await Vue.nextTick();
        expect(wrapper.emitted()["update:key"].length).toBe(1);
        expect(wrapper.emitted()["update:key"][0]).toEqual(["report2Key"]);
    });

    it("typeahead filters list correctly", async () => {
        const wrapper = getWrapper();

        await wrapper.findComponent(VueSelect).setData({open: true});
        await Vue.nextTick();

        (wrapper.findComponent(VueSelect).vm as any).search = "rt2";
        await Vue.nextTick();
        const reportSuggestions = wrapper.findAll("li");
        expect(reportSuggestions.length).toBe(1);
        expect(reportSuggestions.at(0).text()).toContain("report2");
    });

    it("typeahead comes with correct report name preselected", async () => {
        const wrapper = getWrapper("report2Key");

        (wrapper.findComponent(VueSelect).vm.$refs.search as any).focus()
        await Vue.nextTick();

        expect(wrapper.find(".vs__selected").text()).toBe("report2");
    });

});
