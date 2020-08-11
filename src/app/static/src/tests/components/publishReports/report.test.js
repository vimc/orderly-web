import {shallowMount} from "@vue/test-utils";
import dateGroup from "../../../js/components/publishReports/dateGroup";
import report from "../../../js/components/publishReports/report";
import Vue from "vue";
import reportDraft from "../../../js/components/publishReports/reportDraft";

describe("report component", () => {

    const testReport =
        {
            "display_name": "another report",
            "previously_published": true,
            "date_groups": [
                {
                    "date": "Sat Jul 27 2019",
                    "drafts": [{
                        "id": "20190727-123215-97e39008",
                        "url": "http://localhost:8888/other/20190727-123215-97e39008",
                        "changelog": [],
                        "parameter_values": ""
                    }]
                },
                {
                    "date": "Sat Jul 20 2019",
                    "drafts": [{
                        "id": "20190727-201131-d320fa9e",
                        "url": "http://localhost:8888/other/20190720-201131-d320fa9e",
                        "changelog": [],
                        "parameter_values": "nmin=0"
                    }]
                }]
        }

    it("displays date groups", async () => {
        const rendered = shallowMount(report, {propsData: {report: testReport}});
        const groups = rendered.findAll(dateGroup);

        expect(groups.length).toBe(2);
        expect(groups.at(0).props("date")).toBe("Sat Jul 27 2019");
        expect(groups.at(0).props("drafts")).toEqual([{
            "id": "20190727-123215-97e39008",
            "url": "http://localhost:8888/other/20190727-123215-97e39008",
            "changelog": [],
            "parameter_values": ""
        }]);
        expect(groups.at(1).props("date")).toBe("Sat Jul 20 2019");
        expect(groups.at(1).props("drafts")).toEqual([{
            "id": "20190727-201131-d320fa9e",
            "url": "http://localhost:8888/other/20190720-201131-d320fa9e",
            "changelog": [],
            "parameter_values": "nmin=0"
        }]);
    });

    it("emits select-group and select-draft events when checked or un-checked", async () => {
        const rendered = shallowMount(report, {propsData: {report: testReport}});

        rendered.find("input").setChecked(true);

        await Vue.nextTick();

        expect(rendered.emitted("select-draft")[0][0])
            .toEqual({ids: ["20190727-123215-97e39008", "20190727-201131-d320fa9e"], value: true});

        expect(rendered.emitted("select-group")[0][0])
            .toEqual({dates: ["Sat Jul 27 2019", "Sat Jul 20 2019"], value: true});

        rendered.find("input").setChecked(false);

        await Vue.nextTick();

        expect(rendered.emitted("select-draft")[1][0])
            .toEqual({ids: ["20190727-123215-97e39008", "20190727-201131-d320fa9e"], value: false});

        expect(rendered.emitted("select-group")[1][0])
            .toEqual({dates: ["Sat Jul 27 2019", "Sat Jul 20 2019"], value: false});
    });

    it("deselects report if any child is unselected and passes on select-draft event", async () => {
        const rendered = shallowMount(report, {propsData: {report: testReport}});
        rendered.find("input").setChecked(true);

        rendered.find(dateGroup).vm.$emit("select-draft", {id: "20190727-123215-97e39008", value: false});

        expect(rendered.emitted("select-draft")[1][0])
            .toEqual({id: "20190727-123215-97e39008", value: false});

        expect(rendered.vm.$data["selected"]).toBe(false);
    });

    it("passes select-draft event on if any child is selected", () => {
        const rendered = shallowMount(report, {propsData: {report: testReport}});

        rendered.find(dateGroup).vm.$emit("select-draft", {id: "20190727-123215-97e39008", value: true});
        expect(rendered.emitted("select-group")).toBeUndefined();
        expect(rendered.emitted("select-draft")[0][0])
            .toEqual({id: "20190727-123215-97e39008", value: true});
    });

    it("passes select-group event on if any child is selected", () => {
        const rendered = shallowMount(report, {propsData: {report: testReport}});

        rendered.find(dateGroup).vm.$emit("select-group", {date: "Sat Jul 27 2019", value: true});
        expect(rendered.emitted("select-group")[0][0])
            .toEqual({date: "Sat Jul 27 2019", value: true});
    });

});
