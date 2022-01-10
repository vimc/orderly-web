import {shallowMount} from '@vue/test-utils';
import webLink from "../../../js/components/documents/webLink.vue";
import webIcon from "../../../js/components/documents/webIcon";
import fileIcon from "../../../js/components/documents/fileIcon";

describe("webLink", () => {

    it("renders name as link", () => {
        const rendered = shallowMount(webLink, {
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

        expect(rendered.findAll("a").length).toBe(1);
        expect(rendered.findAll("a").at(0).text()).toBe("toplevelfile");
        expect(rendered.findAll("a").at(0).attributes("href")).toBe("toplevelfileurl");
        expect(rendered.findAll("a").at(0).attributes("target")).toBe("_blank");
        expect(rendered.findAll("span").length).toBe(0);
    });

    it("renders web icon", () => {
        const rendered = shallowMount(webLink, {
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

        expect(rendered.findAllComponents(fileIcon).length).toBe(0);
        expect(rendered.findAllComponents(webIcon).length).toBe(1);
    });

});
