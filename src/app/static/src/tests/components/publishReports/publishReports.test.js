import Vue from "vue";
import publishReports from "../../../js/components/publishReports/publishReports";
import {mount, shallowMount} from "@vue/test-utils";
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

    const fakeDraft = {
            "id": "20190824-161244-6e9b57d4",
            "url": "http://localhost:8888/changelog/20190824-161244-6e9b57d4",
            "changelog": [
                {
                    "label": "public",
                    "value": "You think water moves fast? You should see ice. It moves like it has\na mind. Like it knows it killed the world once and got a taste for\nmurder. After the avalanche, it took us a week to climb out. Now, I\ndon't know exactly when we turned on each other, but I know that\nseven of us survived the slide... and only five made it out. Now we\ntook an oath, that I'm breaking now. We said we'd say it was the\nsnow that killed the other two, but it wasn't. Nature is lethal but\nit doesn't hold a candle to man.",
                    "css_class": "public"
                },
                {
                    "label": "internal",
                    "value": "Do you see any Teletubbies in here? Do you see a slender plastic tag\nclipped to my shirt with my name printed on it? Do you see a little\nAsian child with a blank expression on his face sitting outside on a\nmechanical helicopter that shakes when you put quarters in it? No?\nWell, that's what you see at a toy store. And you must think you're in\na toy store, because you're here shopping for an infant named Jeb.",
                    "css_class": "internal"
                }
            ],
            "parameter_values": "nmin=0"
        }

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
    });

    it("can expand all changelogs", async () => {
        const testReportsWithChangelogs = [...testReportsWithDrafts];
        testReportsWithChangelogs[0].date_groups[0].drafts = [fakeDraft];
        testReportsWithChangelogs[1].date_groups[0].drafts = [fakeDraft];

        const rendered = mount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithChangelogs});
        await Vue.nextTick();

        expect(rendered.findAll(".changelog").length).toBe(4);
        expect(rendered.findAll(".changelog").filter(c => c.isVisible()).length).toBe(0);

        const links = rendered.findAll("a");
        links.at(0).trigger("click");

        await Vue.nextTick();

        expect(rendered.findAll(".changelog").filter(c => c.isVisible()).length).toBe(4);

        links.at(1).trigger("click");

        await Vue.nextTick();

        expect(rendered.findAll(".changelog").filter(c => c.isVisible()).length).toBe(0);

    });


    it("displays only previously published reports when option is checked", async () => {
        const rendered = shallowMount(publishReports);
        await Vue.nextTick();
        await Vue.nextTick();
        let reports = rendered.findAll(".report");
        expect(reports.length).toBe(2);
        rendered.find("input").setChecked(true);

        await Vue.nextTick();

        reports = rendered.findAll(".report");
        expect(reports.length).toBe(1);
        expect(reports.at(0).find("h5").text()).toBe("another report");
    });


});
