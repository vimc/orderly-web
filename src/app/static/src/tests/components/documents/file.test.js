import {shallowMount} from '@vue/test-utils';
import File from "../../../js/components/documents/file.vue";
import webIcon from "../../../js/components/documents/webIcon";
import fileIcon from "../../../js/components/documents/fileIcon";

describe("file", () => {

    it("renders name and links", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath.png",
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true,
                    can_open: true
                }
            }
        });

        expect(rendered.findAll("span").at(0).text()).toBe("toplevelfile:");
        expect(rendered.findAll("a").at(0).text()).toBe("open");
        expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl?inline=true");
        expect(rendered.findAll("span").at(1).text()).toBe("/");
        expect(rendered.findAll("a").at(1).text()).toBe("download");
        expect(rendered.findAll("a").at(1).attributes("href")).toBe("toplevelfileurl");
    });

    it("renders file icon if link is local file", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath.png",
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true,
                    external: false
                }
            }
        });

        expect(rendered.findAll(fileIcon).length).toBe(1);
        expect(rendered.findAll(webIcon).length).toBe(0);
    });

    it("renders web icon if link is external", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath.png",
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true,
                    external: true
                }
            }
        });

        expect(rendered.findAll(fileIcon).length).toBe(0);
        expect(rendered.findAll(webIcon).length).toBe(1);
    });

    it("does not render open link when can_open is false", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath.png",
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true,
                    can_open: false
                }
            }
        });

        expect(rendered.findAll("a").length).toBe(1);
        expect(rendered.findAll("a").at(0).text()).toBe("download");
        expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl");
        expect(rendered.findAll("span").length).toBe(1); // the <span>/</span> element should not be present
    });

    it("does not render download link when doc is external", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath.png",
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true,
                    can_open: true,
                    external: true
                }
            }
        });

        expect(rendered.findAll("a").length).toBe(1);
        expect(rendered.findAll("a").at(0).text()).toBe("open");
        expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl");
        expect(rendered.findAll("span").length).toBe(1); // the <span>/</span> element should not be present
    });

});
