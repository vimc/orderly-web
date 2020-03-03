import {shallowMount} from '@vue/test-utils';
import File from "../../../js/components/documents/file.vue";

describe("file", () => {

    it("renders name and links", () => {
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

        expect(rendered.find("span").text()).toBe("toplevelfile:");
        expect(rendered.findAll("a").at(0).text()).toBe("open");
        expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl?inline=true");
        expect(rendered.findAll("a").at(1).text()).toBe("download");
        expect(rendered.findAll("a").at(1).attributes("href")).toBe("toplevelfileurl");
    });

    it("renders open links for openable files only", () => {
        expectOpenLink("test.pdf", true);
        expectOpenLink("test.html", true);
        expectOpenLink("test.htm", true);
        expectOpenLink("test.bmp", true);
        expectOpenLink("test.html", true);
        expectOpenLink("test.jpg", true);
        expectOpenLink("test.jpeg", true);
        expectOpenLink("test.png", true);
        expectOpenLink("test.gif", true);
        expectOpenLink("test.svg", true);

        expectOpenLink("test.doc", false);
    });

    const expectOpenLink = (path, expectLink) => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: path,
                    url: "toplevelfileurl",
                    children: [],
                    is_file: true
                }
            }
        });

        if (expectLink) {
            expect(rendered.findAll("a").at(0).text()).toBe("open");
            expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl?inline=true");
            expect(rendered.findAll("a").at(1).text()).toBe("download");
            expect(rendered.findAll("a").at(1).attributes("href")).toBe("toplevelfileurl");
        } else {
            expect(rendered.findAll("a").length).toBe(1);
            expect(rendered.findAll("a").at(0).text()).toBe("download");
            expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl");
        }
    }

});
