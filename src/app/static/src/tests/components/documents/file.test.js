import {shallowMount} from '@vue/test-utils';
import File from "../../../js/components/documents/file.vue";
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
        expect(rendered.findAll("a").at(0).attributes("target")).toBe("_blank");
        expect(rendered.findAll("span").at(1).text()).toBe("/");
        expect(rendered.findAll("a").at(1).text()).toBe("download");
        expect(rendered.findAll("a").at(1).attributes("href")).toBe("toplevelfileurl");
    });

    it("renders file icon", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath.png",
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true
                }
            }
        });

        expect(rendered.findAll(fileIcon).length).toBe(1);
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

});
