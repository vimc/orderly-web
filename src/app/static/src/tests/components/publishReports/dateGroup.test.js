import dateGroup from "../../../js/components/publishReports/dateGroup";
import {shallowMount} from "@vue/test-utils";
import reportDraft from "../../../js/components/publishReports/reportDraft";

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
        const rendered = shallowMount(dateGroup, {propsData: {date: testDateGroup.date, drafts: testDateGroup.drafts}});
        expect(rendered.find(".date").text()).toBe("Sat Jul 27 2019");
    });

    it("displays drafts", () => {
        const rendered = shallowMount(dateGroup, {propsData: {date: testDateGroup.date, drafts: testDateGroup.drafts}});
        const drafts = rendered.findAll(reportDraft);
        expect(drafts.length).toBe(2);
        expect(drafts.at(0).props("draft")).toEqual(testDateGroup.drafts[0]);
        expect(drafts.at(1).props("draft")).toEqual(testDateGroup.drafts[1]);
    });
});
