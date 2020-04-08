import Vue from "vue";
import {shallowMount} from '@vue/test-utils';
import DocumentList from "../../../js/components/documents/documentList.vue";
import File from "../../../js/components/documents/file.vue";
import WebLink from "../../../js/components/documents/webLink.vue";

describe("document list", () => {

    function getWrapper() {
        return shallowMount(DocumentList, {
            propsData: {
                docs: [{
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
        });
    }

    it("renders folder", () => {
        const wrapper = getWrapper();

        const folderListItem = wrapper.find("li#path");
        expect(folderListItem.classes()).toContain("has-children");
        expect(folderListItem.find("span.folder-name").text()).toBe("folder");
        expect(folderListItem.findAll(".expander").length).toBe(1);
        expect(folderListItem.findAll(File).length).toBe(0);
        expect(folderListItem.findAll(DocumentList).length).toBe(1);
    });

    it("renders file", () => {
        const wrapper = getWrapper();

        const folderListItem = wrapper.find("li#toplevelfilepath");
        expect(folderListItem.classes()).not.toContain("has-children");
        expect(folderListItem.findAll("span.folder-name").length).toBe(0);
        expect(folderListItem.findAll(".expander").length).toBe(0);
        expect(folderListItem.findAll(File).length).toBe(1);
        expect(folderListItem.find(File).vm.$props.doc).toEqual({
            display_name: "toplevelfile",
            path: "toplevelfilepath",
            url: "toplevelfileurl",
            children: [],
            is_file: true,
            external: false
        });
    });

    it("renders web link", () => {
        const wrapper = getWrapper();

        const folderListItem = wrapper.find("li#toplevelexternalpath");
        expect(folderListItem.classes()).not.toContain("has-children");
        expect(folderListItem.findAll("span.folder-name").length).toBe(0);
        expect(folderListItem.findAll(".expander").length).toBe(0);
        expect(folderListItem.findAll(WebLink).length).toBe(1);
        expect(folderListItem.find(WebLink).vm.$props.doc).toEqual({
            display_name: "toplevelexternal",
            path: "toplevelexternalpath",
            url: "toplevelexternalurl",
            children: [],
            is_file: true,
            external: true
        });
    });

    it("renders children", () => {
        const wrapper = getWrapper();

        const folderListItem = wrapper.find("li#path");
        expect(folderListItem.find(DocumentList).vm.$props.docs).toEqual([{
            display_name: "file",
            path: "filepath",
            url: "fileurl",
            children: [],
            is_file: true
        }]);
    });

    it("can toggle folder", async () => {
        const wrapper = getWrapper();

        const folderListItem = wrapper.find("li#path");
        expect(folderListItem.classes()).not.toContain("open");
        expect(folderListItem.find(DocumentList).isVisible()).toBe(false);

        folderListItem.find(".expander").trigger("click");
        await Vue.nextTick();

        expect(folderListItem.classes()).toContain("open");
        expect(folderListItem.find(DocumentList).isVisible()).toBe(true);
    });

});
