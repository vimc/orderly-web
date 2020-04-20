import SetGlobalPinnedReports from "../../../js/components/reports/setGlobalPinnedReports.vue";
import Typeahead from "../../../js/components/typeahead/typeahead";
import ErrorInfo from "../../../js/components/errorInfo";
import {shallowMount} from '@vue/test-utils';
import Vue from "vue";

describe("setGlobalPinnedReports", () => {

    const getWrapper = async function(expanded = true) {
        const result =  shallowMount(SetGlobalPinnedReports, {
            propsData: {
                current: ["r1", "r2"],
                available: {
                    r1: "r1 display",
                    r2: "r2 display",
                    r3: "r3 display"
                }
            }
        });

        if (expanded) {
            result.setData({expanded: true});
            await Vue.nextTick();
        }
        return result;
    };

    it("renders unexpanded by default", async () => {
        const wrapper = await getWrapper(false);
        expect(wrapper.find("#set-pinned-reports-details").exists()).toBe(false);
    });

    it("expands when click link", async () => {
        const wrapper = await getWrapper(false);

        wrapper.find("a").trigger("click");
        await Vue.nextTick();

        expect(wrapper.find("#set-pinned-reports-details").exists()).toBe(true);
    });

    it("renders as expected when expanded", async () => {
        const wrapper = await getWrapper();

        expect(wrapper.find("a").text()).toBe("Edit pinned reports");
        expect(wrapper.find("#set-pinned-reports-details").exists()).toBe(true);

        const li = wrapper.findAll("li");
        expect(li.length).toBe(2);
        expect(li.at(0).find(".name").text()).toBe("r1 display");
        expect(li.at(0).find(".name").attributes().id).toBe("r1");
        expect(li.at(1).find(".name").text()).toBe("r2 display");
        expect(li.at(1).find(".name").attributes().id).toBe("r2");

        expect(wrapper.find(Typeahead).props().data).toEqual(["r3 display"]);
        expect(wrapper.find("#pinned-report-buttons button[type='submit']").text()).toBe("Save changes");
        expect(wrapper.find(".btn-default").text()).toBe("Cancel");

        expect(wrapper.find(ErrorInfo).props().apiError).toBe(null);
        expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("");
    })
});