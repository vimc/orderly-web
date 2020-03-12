import {shallowMount} from "@vue/test-utils";
import {mockAxios} from "../../mockAxios";
import ReportTags from "../../../js/components/reports/reportTags.vue";
import Vue from "vue";

describe("adminApp", () => {
    const propsData = {
        report : {
            name: "r1",
            id: "v1"
        },
        canEdit: false
    };

    const mockTags = {
        version_tags: ["version"],
        report_tags: ["report"],
        orderly_tags: ["orderly"]
    };

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report/r1/version/v1/tags/')
            .reply(200, {"data": mockTags});
    });

    it('fetches tags on mount', async (done) => {
        const wrapper = shallowMount(ReportTags, {propsData: propsData});

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.vm.$data.tags).toStrictEqual(mockTags);
            done();
        });
    });

    it('displays tags sorted and deduped', async () => {
        const wrapper = shallowMount(ReportTags, {propsData: propsData});
        wrapper.setData({
            tags: {
                version_tags: ["b"],
                report_tags: ["c", "a"],
                orderly_tags: ["a", "b"]
            }
        });

        await Vue.nextTick();

        const tags = wrapper.findAll("span");
        expect(tags.length).toBe(3);
        expect(tags.at(0).text()).toBe("a");
        expect(tags.at(1).text()).toBe("b");
        expect(tags.at(2).text()).toBe("c");
    });

    it('displays nothing if no tags are present', async() => {
        const wrapper = shallowMount(ReportTags, {propsData: propsData});
        wrapper.setData({
            tags: {
                version_tags: [],
                report_tags: [],
                orderly_tags: []
            }
        });

        await Vue.nextTick();

        const div = wrapper.findAll("div");
        expect(div.length).toBe(0);
    });
});