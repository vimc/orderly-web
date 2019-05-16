import {expect} from "chai";
import {describe} from "mocha";
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

        expect(wrapper.find('[data-toggle="toggle"]').classes()).to.contain("off");
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

        expect(wrapper.find('[data-toggle="toggle"]').classes()).not.to.contain("off");
    });

    it('emits toggle event after successful publish toggle', (done) => {
        mockAxios.onPost('/reports/name1/versions/version/publish/')
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
            expect(mockAxios.history.post.length).to.eq(1);
            expect(wrapper.emitted().toggle).to.not.eq(undefined);
            expect(wrapper.find(".alert.alert-danger").exists()).to.be.false;
            done();
        });

    });

    it('does not emit toggle event after failed publish toggle', (done) => {
        mockAxios.onPost('/reports/name1/versions/version/publish/')
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
            expect(mockAxios.history.post.length).to.eq(1);
            expect(wrapper.emitted().toggle).to.eq(undefined);
            expect(wrapper.find(".alert.alert-danger").text()).to.eq("Could not toggle status");
            done();
        });

    })
});
