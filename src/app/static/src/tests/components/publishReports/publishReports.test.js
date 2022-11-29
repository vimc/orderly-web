import Vue from "vue";
import errorInfo from "../../../js/components/errorInfo";
import publishReports from "../../../js/components/publishReports/publishReports";
import report from "../../../js/components/publishReports/report";
import {mount, shallowMount} from "@vue/test-utils";
import {mockAxios} from "../../mockAxios";

describe("publishReports", () => {

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

    const testReportsWithDrafts = [
        {
            "display_name": "another report",
            "previously_published": true,
            "date_groups": [
                {
                    "date": "Sat Jul 27 2019",
                    "drafts": [{...fakeDraft, id: "20191024-161244-6e9b57d4"}]
                },
                {
                    "date": "Sat Jul 20 2019",
                    "drafts": [{...fakeDraft, id: "20190924-161244-6e9b57d4"}]
                }
            ]
        },
        {
            "display_name": "global",
            "previously_published": false,
            "date_groups": [
                {
                    "date": "Mon Jul 29 2019",
                    "drafts": [fakeDraft]
                }
            ]
        }]

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report-drafts/')
            .reply(200, {"data": testReportsWithDrafts});
        mockAxios.onPost('http://app/bulk-publish/')
            .reply(200);
    });

    it("loads reports on mount", async () => {
        const rendered = shallowMount(publishReports);
        await Vue.nextTick(); // once for axios to return
        await Vue.nextTick(); // once for date to update
        await Vue.nextTick();
        expect(mockAxios.history.get.length).toBe(1);
        expect(rendered.findAllComponents(report).length).toBe(2);
    });

    it("displays reports", async () => {
        const rendered = shallowMount(publishReports);
        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();
        const reports = rendered.findAllComponents(report);
        expect(reports.length).toBe(2);
        expect(reports.at(0).props()).toEqual({
            report: testReportsWithDrafts[0],
            selectedIds: {
                "20190824-161244-6e9b57d4": false,
                "20191024-161244-6e9b57d4": false,
                "20190924-161244-6e9b57d4": false

            },
            selectedDates: {
                "Mon Jul 29 2019": false,
                "Sat Jul 20 2019": false,
                "Sat Jul 27 2019": false
            },
            expandClicked: 0,
            collapseClicked: 0
        });
    });

    it("renders title and help text", () => {
        const rendered = shallowMount(publishReports);
        expect(rendered.find("h1.h3").text()).toBe("Publish reports");
        expect(rendered.find("span.text-muted").text())
            .toBeIgnoringWhitespace("Here you can publish the latest drafts (unpublished versions) of reports in bulk." +
                "You can also manage the publish status of an individual report version directly from its report page.");
    });

    it("can expand all changelogs", async () => {
        const rendered = mount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();

        expect(rendered.findAll(".changelog").length).toBe(6);
        expect(rendered.findAll(".changelog").filter(c => c.isVisible()).length).toBe(0);

        const links = rendered.findAll("a");
        links.at(0).trigger("click");

        await Vue.nextTick();

        expect(rendered.findAll(".changelog").filter(c => c.isVisible()).length).toBe(6);

        links.at(1).trigger("click");

        await Vue.nextTick();

        expect(rendered.findAll(".changelog").filter(c => c.isVisible()).length).toBe(0);

    });

    it("displays only previously published reports when option is checked", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();
        let reports = rendered.findAllComponents(report);
        expect(reports.length).toBe(2);
        rendered.find("input").setChecked(true);

        await Vue.nextTick();

        reports = rendered.findAllComponents(report);
        expect(reports.length).toBe(1);
        expect(reports.at(0).props("report")).toEqual(testReportsWithDrafts[0]);
    });

    it("updates selectedIds and selectedDates when only previously published reports option is checked", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();

        rendered.findComponent(report).vm.$emit("select-draft", {id: "20190824-161244-6e9b57d4", value: true});
        expect(rendered.vm.$data["selectedIds"]["20190824-161244-6e9b57d4"]).toBe(true);
        rendered.findComponent(report).vm.$emit("select-group", {date: "Mon Jul 29 2019", value: true});
        expect(rendered.vm.$data["selectedDates"]["Mon Jul 29 2019"]).toBe(true);

        rendered.find("input").setChecked(true);

        await Vue.nextTick();

        expect(rendered.vm.$data["selectedIds"]["20190824-161244-6e9b57d4"]).toBe(false);
        expect(rendered.vm.$data["selectedDates"]["Mon Jul 29 2019"]).toBe(false);
    });

    it("updates selectedIds when select-draft event with single id is emitted", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();
        rendered.findComponent(report).vm.$emit("select-draft", {id: "20190727-123215-97e39008", value: true});
        expect(rendered.vm.$data["selectedIds"]["20190727-123215-97e39008"]).toBe(true);
    });

    it("updates selectedIds when select-draft event with multiple ids is emitted", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();
        rendered.findComponent(report).vm.$emit("select-draft",
            {
                ids: ["20190727-123215-97e39008", "20190727-201131-d320fa9e"],
                value: true
            });
        expect(rendered.vm.$data["selectedIds"]["20190727-123215-97e39008"]).toBe(true);
        expect(rendered.vm.$data["selectedIds"]["20190727-201131-d320fa9e"]).toBe(true);
    });

    it("updates selectedDates when select-group event with single date is emitted", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();
        rendered.findComponent(report).vm.$emit("select-group", {date: "Sat Jul 27 2019", value: true});
        expect(rendered.vm.$data["selectedDates"]["Sat Jul 27 2019"]).toBe(true);
    });

    it("updates selectedDates when select-group event with multiple dates is emitted", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();
        rendered.findComponent(report).vm.$emit("select-group",
            {
                dates: ["Sat Jul 27 2019", "Sun Jul 28 2019"],
                value: true
            });
        expect(rendered.vm.$data["selectedDates"]["Sat Jul 27 2019"]).toBe(true);
        expect(rendered.vm.$data["selectedDates"]["Sun Jul 28 2019"]).toBe(true);
    });

    it("publishes selected reports and refreshes reports", async () => {
        const rendered = shallowMount(publishReports);
        rendered.setData({reportsWithDrafts: testReportsWithDrafts});
        await Vue.nextTick();
        rendered.findComponent(report).vm.$emit("select-draft",
            {
                id: "20190824-161244-6e9b57d4",
                value: true
            });

        expect(rendered.vm.$data["selectedIds"]["20190824-161244-6e9b57d4"]).toBe(true);

        const btn = rendered.find("button");
        await btn.trigger("click");
        expect(btn.text()).toBe("Publish");
        await Vue.nextTick();

        expect(mockAxios.history.post[0].data).toEqual(JSON.stringify({ids: ["20190824-161244-6e9b57d4"]}));

        await Vue.nextTick();
        expect(mockAxios.history.get.length).toBe(2);

        await Vue.nextTick();

        // because the data has been refreshed with the same fake data as before, this will now be false again
        expect(rendered.vm.$data["selectedIds"]["20190824-161244-6e9b57d4"]).toBe(false);
    });

    it("displays error message if publishing fails", async () => {

        mockAxios.onPost('http://app/bulk-publish/')
            .reply(500, "TEST ERROR");

        const rendered = shallowMount(publishReports);
        const btn = rendered.find("button");
        await btn.trigger("click");

        await Vue.nextTick(); // once to trigger click
        await Vue.nextTick(); // once for api to return
        await Vue.nextTick(); // once for props to update
        await Vue.nextTick();

        expect(rendered.findComponent(errorInfo).props().apiError.response.data).toBe("TEST ERROR")
        expect(rendered.findComponent(errorInfo).props().defaultMessage)
            .toBe("Something went wrong. Please try again or contact support.");

        // error should be cleared after a successful publish
        mockAxios.onPost('http://app/bulk-publish/')
            .reply(200);

        btn.trigger("click");

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(rendered.findComponent(errorInfo).props()).toEqual({
            apiError: null,
            defaultMessage: "Something went wrong. Please try again or contact support."
        });

    });

});
