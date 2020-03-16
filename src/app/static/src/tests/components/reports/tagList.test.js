import TagList from "../../../js/components/reports/tagList";
import {shallowMount} from '@vue/test-utils';

describe("tagList", () => {
    const propsData = {
        editable: true,
        value: ["tag1", "tag2"],
        header: "Tag List",
        description: "Here are some tags"
    };

    it('renders as expected when editable', () => {
        const wrapper = shallowMount(TagList, {propsData});
        expect(wrapper.find("h6").text()).toBe("Tag List");
        expect(wrapper.find(".tag-list-description").text()).toBe("Here are some tags");

        const tags = wrapper.findAll(".badge");
        expect(tags.length).toBe(2);
        expect(tags.at(0).text()).toBeIgnoringWhitespace("tag1 | ×");
        expect(tags.at(0).classes().indexOf("badge-primary")).toBeGreaterThan(-1);
        expect(tags.at(1).text()).toBeIgnoringWhitespace("tag2 | ×");

        expect(wrapper.find(".input-group button").text()).toBe("Add tag");
    });

    it('renders as expected when noteditable', () => {
        const wrapper = shallowMount(TagList, {propsData: {...propsData, editable: false}});
        expect(wrapper.find("h6").text()).toBe("Tag List");
        expect(wrapper.find(".tag-list-description").text()).toBe("Here are some tags");

        const tags = wrapper.findAll(".badge");
        expect(tags.length).toBe(2);
        expect(tags.at(0).text()).toBeIgnoringWhitespace("tag1");
        expect(tags.at(0).classes().indexOf("badge-secondary")).toBeGreaterThan(-1);
        expect(tags.at(1).text()).toBeIgnoringWhitespace("tag2");

        expect(wrapper.findAll(".input-group").length).toBe(0);
    });

    it('adding tag emits event with new tag list', () => {
        const wrapper = shallowMount(TagList, {propsData});

        wrapper.find("input").setValue("tag3");
        wrapper.find("button").trigger("click");
        expect(wrapper.emitted().input.length).toBe(1);
        expect(wrapper.emitted("input")[0][0]).toStrictEqual(["tag1", "tag2", "tag3"]);
    });

    it('attempting to add empty tag does not emit event', () => {
        const wrapper = shallowMount(TagList, {propsData});

        wrapper.find("button").trigger("click");
        expect(wrapper.emitted().input).toBeUndefined();
    });

    it('attempting to add tag which is already in list does not emit event', () => {
        const wrapper = shallowMount(TagList, {propsData});

        wrapper.find("input").setValue("tag1");
        wrapper.find("button").trigger("click");
        expect(wrapper.emitted().input).toBeUndefined();
    });

    it('deleting tag emits event with new tag list', () => {
        const wrapper = shallowMount(TagList, {propsData});

        wrapper.find(".remove-tag").trigger("click");
        expect(wrapper.emitted().input.length).toBe(1);
        expect(wrapper.emitted("input")[0][0]).toStrictEqual(["tag2"]);
    });
});