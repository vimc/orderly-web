import {shallowMount} from "@vue/test-utils";
import Vue from "vue";
import ReportDependencyList from "../../../js/components/reports/reportDependencyList";

describe("reportDependencyList", () => {

    const testDependencyList = [
        {
            id: "report-id-1",
            name: "report-name-1",
            out_of_date: true,
            dependencies: []
        },
        {
            id: "report-id-2",
            name: "report-name-2",
            out_of_date: false,
            dependencies: [
                {
                    id: "report-id-1",
                    name: "report-name-2",
                    out_of_date: true,
                    dependencies: []
                }
            ]
        },
    ];

    const getWrapper = function(dependencyList) {
        return shallowMount(ReportDependencyList, {
            propsData: { dependencyList }
        });
    };

    it("renders as expected", () => {
        const wrapper = getWrapper(testDependencyList);
        const ul = wrapper.find("ul");
        const listItems = ul.findAll("li");
        expect(listItems.length).toBe(2);
        const li1 = listItems.at(0);
        expect(li1.classes()).toStrictEqual([]);
        expect(li1.find("div.expander").exists()).toBe(false);
        expect(li1.find("span.report-dependency-item").text()).toBe("report-name-1 (report-id-1)");
        expect(li1.find("span.report-dependency-item a").attributes("href")).toBe("/report/report-name-1/report-id-1");
        expect(li1.find("report-dependency-list-stub").exists()).toBe(false);
        const li2 = listItems.at(1);
        expect(li2.classes()).toStrictEqual(["has-children"]);
        expect(li2.find("div.expander").exists()).toBe(true);
        expect(li2.find("span.report-dependency-item").text()).toBe("report-name-2 (report-id-2)");
        expect(li2.find("span.report-dependency-item a").attributes("href")).toBe("/report/report-name-2/report-id-2");
        expect(li2.find("report-dependency-list-stub").props("dependencyList")).toBe(testDependencyList[1].dependencies);
        expect(li2.find("report-dependency-list-stub").element.style.display).toBe("none");
    });

    it("click on expander toggles open style and shows child component", async () => {
        const wrapper = getWrapper(testDependencyList);
        const li = wrapper.findAll("li").at(1);
        const expander = li.find("div.expander");
        expander.trigger("click");
        await Vue.nextTick();
        expect(li.classes()).toStrictEqual(["has-children", "open"]);
        expect(li.find("report-dependency-list-stub").element.style.display).toBe("");
        expander.trigger("click");
        await Vue.nextTick();
        expect(li.classes()).toStrictEqual(["has-children"]);
        expect(li.find("report-dependency-list-stub").element.style.display).toBe("none");
    });

    it("click on list item span applies open style and shows child component", async () => {
        const wrapper = getWrapper(testDependencyList);
        const li = wrapper.findAll("li").at(1);
        const span = li.find("span.report-dependency-item");
        span.trigger("click");
        await Vue.nextTick();
        expect(li.classes()).toStrictEqual(["has-children", "open"]);
        expect(li.find("report-dependency-list-stub").element.style.display).toBe("");
        span.trigger("click");
        await Vue.nextTick();
        expect(li.classes()).toStrictEqual(["has-children"]);
        expect(li.find("report-dependency-list-stub").element.style.display).toBe("none");
    });
});