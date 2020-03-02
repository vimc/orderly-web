import {shallowMount} from '@vue/test-utils';
import File from "../../../js/components/documents/file.vue";

describe("file", () => {

    it("renders name and links", () => {
        const rendered = shallowMount(File, {
            propsData: {
                doc: {
                    display_name: "toplevelfile",
                    path: "toplevelfilepath",
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

});
