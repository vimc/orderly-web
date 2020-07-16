import Vue from "vue";
import publishReports from "../../../js/components/publishReports/publishReports";
import {shallowMount} from "@vue/test-utils";
import dateGroup from "../../../js/components/publishReports/dateGroup";
import {mockAxios} from "../../mockAxios";

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

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report-drafts/')
            .reply(200, {"data": testReportsWithDrafts});
    });

    it("loads reports on mount", async () => {
        const rendered = shallowMount(publishReports);
        await Vue.nextTick(); // once for axios to return
        await Vue.nextTick(); // once for date to update
        expect(mockAxios.history.get.length).toBe(1);
        expect(rendered.findAll(".report").length).toBe(2);
    })

    it("displays report names", async () => {
        const rendered = shallowMount(publishReports);
        await Vue.nextTick();
        await Vue.nextTick();
        const reports = rendered.findAll(".report");
        expect(reports.length).toBe(2);
        expect(reports.at(0).find("h5").text()).toBe("another report");
        expect(reports.at(1).find("h5").text()).toBe("global");
    });

    it("displays date groups", async () => {
        const rendered = shallowMount(publishReports);
        await Vue.nextTick();
        await Vue.nextTick();
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

    it("renders title and help text", () => {
        const rendered = shallowMount(publishReports);
        expect(rendered.find("h1.h3").text()).toBe("Publish reports");
        expect(rendered.find("span.text-muted").text())
            .toBeIgnoringWhitespace("Here you can publish the latest drafts (unpublished versions) of reports in bulk." +
            "You can also manage the publish status of an individual report version directly from its report page.");
    })

});
