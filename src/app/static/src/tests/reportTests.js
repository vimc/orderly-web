import {expect} from "chai";
import {describe} from "mocha";
import axios from "axios";
import {mount} from '@vue/test-utils'
import MockAdapter from "axios-mock-adapter";
import PublishSwitch from "../js/components/reports/publishSwitch.vue";
import RunReport from "../js/components/reports/runReport.vue"

describe('report page', () => {

    const mockAxios = new MockAdapter(axios);

    beforeEach(() => {
        mockAxios.reset();
    });

    describe("publishSwitch", () => {

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

    describe("runReport", () => {
        it('shows run button only when no run status', () => {

            const wrapper = mount(RunReport, {
                propsData: {
                    report: {
                        name: "name1",
                    }
                }
            });

            expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");
            expect(wrapper.find("#run-report-status").exists()).to.be.false;
        });
    })

});
