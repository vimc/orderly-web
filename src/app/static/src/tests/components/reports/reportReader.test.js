import ReportReader from "../../../js/components/reports/permissions/reportReader.vue"
import {mount} from "@vue/test-utils";

describe("reportReadersList", () => {

    it("displays removable report reader", () => {

        const wrapper = mount(ReportReader, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        expect(wrapper.find('span.reader-display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll('span.remove-reader').length).toBe(1);
    });

    it("displays non-removable report reader", () => {

        const wrapper = mount(ReportReader, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: false
            }
        });

        expect(wrapper.find('span.reader-display-name').text()).toBe("Test User");
        expect(wrapper.find('.email').text()).toBe("test.user@example.com");
        expect(wrapper.findAll('span.remove-reader').length).toBe(0);
    });

    it("emits remove event", () => {

        const wrapper = mount(ReportReader, {
            propsData: {
                email: "test.user@example.com",
                displayName: "Test User",
                canRemove: true
            }
        });

        wrapper.find('span.remove-reader').trigger("click");
        expect(wrapper.emitted().remove[0])
            .toEqual(expect.arrayContaining(["test.user@example.com"]))
    })
});