import Vue from "vue";
import refreshDocuments from "../../../js/components/documents/refreshDocuments";
import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import DocumentList from "../../../js/components/documents/documentList";
import DocumentPage from "../../../js/components/documents/documentPage";
import RefreshDocuments from "../../../js/components/documents/refreshDocuments";

describe("document page", () => {

    function getDocs() {
        return [{
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
            }
        ]
    }

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet("http://app/documents/")
            .reply(200, {
                "data": getDocs()
            });
    });

    function getWrapper(canManage) {
        return shallowMount(DocumentPage, {
            propsData: {
                canManage
            }
        });
    }

    it("includes refresh documents widget if canManage is true", () => {
        const wrapper = getWrapper(true);
        expect(wrapper.findAll(RefreshDocuments).length).toBe(1);
    });

    it("does not include refresh documents widget if canManage is false", () => {
        const wrapper = getWrapper(false);
        expect(wrapper.findAll(RefreshDocuments).length).toBe(0);
    });

    it("fetches documents on load", async () => {
        const wrapper = getWrapper();

        await Vue.nextTick();

        expect(mockAxios.history.get.length).toBe(1);
        expect(mockAxios.history.get[0].url).toBe("http://app/documents/");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.find(DocumentList).vm.$props.docs).toEqual(getDocs())
    });

    it("fetches documents after refresh", async () => {
        const wrapper = getWrapper(true);

        await Vue.nextTick();

        expect(mockAxios.history.get.length).toBe(1);

        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.find(DocumentList).vm.$props.docs).toEqual(getDocs());

        mockAxios.onGet("http://app/documents/")
            .reply(200, {
                "data": []
            });

        wrapper.find(RefreshDocuments).vm.$emit("refreshed");

        await Vue.nextTick();

        expect(mockAxios.history.get.length).toBe(2);

        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.find(DocumentList).vm.$props.docs).toEqual([]);
    });

});
