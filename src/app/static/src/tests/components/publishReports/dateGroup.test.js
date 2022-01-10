import dateGroup from "../../../js/components/publishReports/dateGroup";
import {shallowMount} from "@vue/test-utils";
import reportDraft from "../../../js/components/publishReports/reportDraft";
import Vue from "vue";

describe("dateGroup", () => {
    const testDateGroup = {
        "date": "Sat Jul 27 2019",
        "drafts": [
            {
                "id": "20190727-123215-97e39008",
                "url": "http://localhost:8888/other/20190727-123215-97e39008",
                "changelog": [],
                "parameter_values": ""
            },
            {
                "id": "20190727-201131-d320fa9e",
                "url": "http://localhost:8888/other/20190720-201131-d320fa9e",
                "changelog": [],
                "parameter_values": "nmin=0"
            }
        ]
    }

    it("displays date", () => {
        const rendered = shallowMount(dateGroup, {
            propsData: {
                date: testDateGroup.date, drafts: testDateGroup.drafts,
                selectedIds: {}, selectedDates: {}
            }
        });
        expect(rendered.find(".h6").text()).toBe("Sat Jul 27 2019");
    });

    it("displays drafts", () => {
        const rendered = shallowMount(dateGroup, {
            propsData: {
                date: testDateGroup.date,
                drafts: testDateGroup.drafts,
                selectedIds: {},
                selectedDates: {}
            }
        });
        const drafts = rendered.findAllComponents(reportDraft);
        expect(drafts.length).toBe(2);
        expect(drafts.at(0).props("draft")).toEqual(testDateGroup.drafts[0]);
        expect(drafts.at(1).props("draft")).toEqual(testDateGroup.drafts[1]);
    });

    it("input is checked based on selectedDates", async () => {
        const rendered = shallowMount(dateGroup, {
            propsData: {
                selectedDates: {"Sat Jul 27 2019": false},
                date: testDateGroup.date,
                drafts: testDateGroup.drafts,
                selectedIds: {}
            }
        });

        expect(rendered.find("input").element.checked).toBe(false);

        rendered.setProps({selectedDates: {"Sat Jul 27 2019": true}});

        await Vue.nextTick();

        expect(rendered.find("input").element.checked).toBe(true);
    });

    it("emits select-group and select-draft events when checked or un-checked", async () => {
        const rendered = shallowMount(dateGroup, {
            propsData: {
                date: testDateGroup.date,
                drafts: testDateGroup.drafts,
                selectedIds: {},
                selectedDates: {}
            }
        });

        rendered.find("input").setChecked(true);

        await Vue.nextTick();

        expect(rendered.emitted("select-draft")[0][0])
            .toEqual({ids: ["20190727-123215-97e39008", "20190727-201131-d320fa9e"], value: true});

        expect(rendered.emitted("select-group")[0][0])
            .toEqual({date: "Sat Jul 27 2019", value: true});

        rendered.find("input").setChecked(false);

        await Vue.nextTick();

        expect(rendered.emitted("select-draft")[1][0])
            .toEqual({ids: ["20190727-123215-97e39008", "20190727-201131-d320fa9e"], value: false});

        expect(rendered.emitted("select-group")[1][0])
            .toEqual({date: "Sat Jul 27 2019", value: false});
    });

    it("deselects group if any child is unselected and passes on select-draft event", () => {
        const rendered = shallowMount(dateGroup, {
            propsData: {
                date: testDateGroup.date,
                drafts: testDateGroup.drafts,
                selectedIds: {},
                selectedDates: {}
            }
        });

        rendered.findComponent(reportDraft).vm.$emit("select-draft", {id: "20190727-123215-97e39008", value: false});
        expect(rendered.emitted("select-group")[0][0])
            .toEqual({date: "Sat Jul 27 2019", value: false});
        expect(rendered.emitted("select-draft")[0][0])
            .toEqual({id: "20190727-123215-97e39008", value: false});
    });

    it("passes select-draft event on if any child is selected", () => {
        const rendered = shallowMount(dateGroup, {
            propsData: {
                date: testDateGroup.date,
                drafts: testDateGroup.drafts,
                selectedIds: {},
                selectedDates: {}
            }
        });

        rendered.findComponent(reportDraft).vm.$emit("select-draft", {id: "20190727-123215-97e39008", value: true});
        expect(rendered.emitted("select-group")).toBeUndefined();
        expect(rendered.emitted("select-draft")[0][0])
            .toEqual({id: "20190727-123215-97e39008", value: true});
    });

});
