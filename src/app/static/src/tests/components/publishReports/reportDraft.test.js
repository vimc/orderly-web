import {shallowMount} from "@vue/test-utils";
import reportDraft from "../../../js/components/publishReports/reportDraft";

describe("reportDraft", () => {

    const fakeDraft =  {
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

    it("displays link to report", () => {
        const rendered = shallowMount(reportDraft, {propsData: {draft: fakeDraft}});
        expect(rendered.find("a").text()).toBe("20190824-161244-6e9b57d4");
        expect(rendered.find("a").attributes("href")).toBe("http://localhost:8888/changelog/20190824-161244-6e9b57d4");
    });

    it("displays parameters", () => {
        const rendered = shallowMount(reportDraft, {propsData: {draft: fakeDraft}});
        expect(rendered.find("span").text()).toBe("nmin=0");
    });

    it("displays changelogs", () => {
        const rendered = shallowMount(reportDraft, {propsData: {draft: fakeDraft}});
        const logs = rendered.findAll(".changelog")
        expect(logs.length).toBe(2);
        expect(logs.at(0).find(".badge-public").text()).toBe("public");
        expect(logs.at(0).find(".public").text()).toBe(fakeDraft.changelog[0].value);
        expect(logs.at(1).find(".badge-internal").text()).toBe("internal");
        expect(logs.at(1).find(".internal").text()).toBe(fakeDraft.changelog[1].value);
    });
});
