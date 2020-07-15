import publishReports from "../../../js/components/publishReports/publishReports";
import {shallowMount} from "@vue/test-utils";
import dateGroup from "../../../js/components/publishReports/dateGroup";

describe("publishReports", () => {

    const testReportsWithDrafts = [
        {
            "display_name": "another report",
            "previously_published": true,
            "date_groups": [
                {
                    "date": "Sat Jul 27 2019",
                    "drafts": []
                },
                {
                    "date": "Sat Jul 20 2019",
                    "drafts": []
                }
            ]
        },
        {
            "display_name": "global",
            "previously_published": false,
            "date_groups": [
                {
                    "date": "Mon Jul 29 2019",
                    "drafts": []
                }
            ]
        }]

    it("displays report names", () => {
        const rendered = shallowMount(publishReports, {propsData: {reportsWithDrafts: testReportsWithDrafts}});
        const reports = rendered.findAll(".report");
        expect(reports.length).toBe(2);
        expect(reports.at(0).find("span").text()).toBe("another report");
        expect(reports.at(1).find("span").text()).toBe("global");
    });

    it("displays date groups", () => {
        const rendered = shallowMount(publishReports, {propsData: {reportsWithDrafts: testReportsWithDrafts}});

        const reports = rendered.findAll(".report");
        let groups = reports.at(0).findAll(dateGroup);

        expect(groups.length).toBe(2);
        expect(groups.at(0).props("date")).toBe("Sat Jul 27 2019");
        expect(groups.at(0).props("drafts")).toEqual([]);
        expect(groups.at(1).props("date")).toBe("Sat Jul 20 2019");
        expect(groups.at(1).props("drafts")).toEqual([]);

        groups = reports.at(1).findAll(dateGroup);

        expect(groups.length).toBe(1);
        expect(groups.at(0).props("date")).toBe("Mon Jul 29 2019");
        expect(groups.at(0).props("drafts")).toEqual([]);
    });

    it("renders title", () => {
        const rendered = shallowMount(publishReports, {propsData: {reportsWithDrafts: testReportsWithDrafts}});
        expect(rendered.find("h2").text()).toBe("Latest drafts")
    })

});
