import Vue from "vue";
import {shallowMount, mount} from '@vue/test-utils';
import DocumentList from "../../../js/components/documents/documentList.vue";
import File from "../../../js/components/documents/file.vue";
import WebLink from "../../../js/components/documents/webLink.vue";

describe("document list", () => {

    const propsData = {
        docs: [
            {
                display_name: "folder",
                path: "path",
                url: "url",
                children: [{
                    display_name: "file",
                    path: "filepath",
                    url: "fileurl",
                    children: [],
                    is_file: true
                }],
                is_file: false
            },
            {
                display_name: "toplevelfile",
                path: "toplevelfilepath",
                url: "toplevelfileurl",
                children: [],
                is_file: true,
                external: false
            },
            {
                display_name: "toplevelexternal",
                path: "toplevelexternalpath",
                url: "toplevelexternalurl",
                children: [],
                is_file: true,
                external: true
            }]
    }

    it("renders folder", () => {
        const wrapper = shallowMount(DocumentList, {propsData});
        const folderListItem = wrapper.find("li#path");
        expect(folderListItem.classes()).toContain("has-children");
        expect(folderListItem.find("span.folder-name").text()).toBe("folder");
        expect(folderListItem.findAll(".expander").length).toBe(1);
        expect(folderListItem.findAll("file-stub").length).toBe(0);
        expect(folderListItem.findAll("document-list-stub").length).toBe(1);
    });

    it("renders file", () => {
        const wrapper = mount(DocumentList, {propsData});

        const folderListItem = wrapper.find("li#toplevelfilepath");
        expect(folderListItem.classes()).not.toContain("has-children");
        expect(folderListItem.findAll("span.folder-name").length).toBe(0);
        expect(folderListItem.findAll(".expander").length).toBe(0);
        expect(folderListItem.findAll("a").length).toBe(1);
        expect(folderListItem.find("a").attributes("href")).toBe("toplevelfileurl");
        expect(folderListItem.find("a").text()).toBe("download");
    });

    it("renders web link", () => {
        const wrapper = mount(DocumentList, {propsData});

        const folderListItem = wrapper.find("li#toplevelexternalpath");
        expect(folderListItem.classes()).not.toContain("has-children");
        expect(folderListItem.findAll("span.folder-name").length).toBe(0);
        expect(folderListItem.findAll(".expander").length).toBe(0);
        expect(folderListItem.find("a").attributes("href")).toBe("toplevelexternalurl");
        expect(folderListItem.find("a").text()).toBe("toplevelexternal");
    });

    it("renders children", () => {
        const wrapper = shallowMount(DocumentList, {propsData});
        expect(wrapper.findAllComponents(DocumentList).at(1).vm.$props.docs).toEqual([{
            display_name: "file",
            path: "filepath",
            url: "fileurl",
            children: [],
            is_file: true
        }]);
    });

    it("can toggle folder", async () => {
        const wrapper = shallowMount(DocumentList, {propsData});

        const folderListItem = wrapper.find("li#path");
        expect(folderListItem.classes()).not.toContain("open");
        expect(wrapper.findAllComponents(DocumentList).at(1).isVisible()).toBe(false);

        folderListItem.find(".expander").trigger("click");
        await Vue.nextTick();

        expect(folderListItem.classes()).toContain("open");
        expect(wrapper.findAllComponents(DocumentList).at(1).isVisible()).toBe(true);
    });

});
