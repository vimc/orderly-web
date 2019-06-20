import {mount} from '@vue/test-utils';
import {mockAxios} from "../../mockAxios";
import PublishSwitch from "../../../js/components/reports/publishSwitch.vue";

describe("publishSwitch", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    it('shows switch as off when report is unpublished', () => {

        const wrapper = mount(PublishSwitch, {
            propsData: {
                report: {
                    name: "name1",
                    id: "version",
                    published: false
                }
            }
        });

        expect(wrapper.find('[data-toggle="toggle"]').classes()).toContain("off");
    });

    it('shows switch as on when report is published', () => {

        const wrapper = mount(PublishSwitch, {
            propsData: {
                report: {
                    name: "name1",
                    id: "version",
                    published: true
                }
            }
        });

        expect(wrapper.find('[data-toggle="toggle"]').classes()).not.toContain("off");
    });

    it('emits toggle event after successful publish toggle', (done) => {
        mockAxios.onPost('http://app/report/name1/version/version/publish/')
            .reply(200);

        const wrapper = mount(PublishSwitch, {
            propsData: {
                report: {
                    name: "name1",
                    id: "version",
                    published: false
                }
            }
        });

        wrapper.find('[data-toggle="toggle"]').trigger("click");
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(wrapper.emitted().toggle).toBeDefined();
            expect(wrapper.find(".text-danger").exists()).toBe(false);
            done();
        });

    });

    it('does not emit toggle event after failed publish toggle', (done) => {
        mockAxios.onPost('http://app/report/name1/version/version/publish/')
            .reply(500);

        const wrapper = mount(PublishSwitch, {
            propsData: {
                report: {
                    name: "name1",
                    id: "version",
                    published: false
                }
            }
        });

        wrapper.find('[data-toggle="toggle"]').trigger("click");

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(wrapper.emitted().toggle).toBeUndefined();
            expect(wrapper.find(".text-danger").text()).toBe("Error: could not toggle status");
            done();
        });

    })
});
