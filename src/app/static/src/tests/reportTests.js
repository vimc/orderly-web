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

        const runReportProps = {
            propsData: {
                report: {
                    name: "name1",
                }
            }
        };

        it('shows run button only when no run status', () => {

            const wrapper = mount(RunReport, runReportProps);

            expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");
            expect(wrapper.find("#run-report-status").exists()).to.eq(false);
        });

        it('shows status', () => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.setData({
                showModal: false,
                pollingTimer: null,
                runningKey: "some_key",
                runningStatus: "some_status",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            });

            expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");
            expect(wrapper.find("#run-report-status").text()).to.contain("Running status: some_status");

            expect(wrapper.find("#run-report-new-version").exists()).to.eq(false);
            expect(wrapper.find("#run-report-dismiss").text()).to.eq("Dismiss");
        });

        it('shows new version', () => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.setData({
                showModal: false,
                pollingTimer: null,
                runningKey: "some_key",
                runningStatus: "some_status",
                newVersionFromRun: "new_version",
                newVersionDisplayName: "A new version"
            });

            expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");
            expect(wrapper.find("#run-report-status").text()).to.contain("Running status: some_status");

            expect(wrapper.find("#run-report-new-version").text()).to.eq("New version: A new version");
            expect(wrapper.find("#run-report-new-version a").attributes("href")).to.eq("/reports/name1/new_version");
            expect(wrapper.find("#run-report-dismiss").text()).to.eq("Dismiss");
        });

        it('shows modal', () => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.setData({
                showModal: true,
                pollingTimer: null,
                runningKey: "",
                runningStatus: "",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            });

            expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-show");
            expect(wrapper.find("#run-report-status").exists()).to.eq(false);


        });

        it('displays modal when run button is pressed', () => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.find('button[type="submit"]').trigger("click");

            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-show");

        });

        it('posts run request when confirm run button is pressed', (done) => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.setData({
                showModal: true,
                pollingTimer: null,
                runningKey: "",
                runningStatus: "",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            });

            wrapper.find("#confirm-run-btn").trigger("click");

            setTimeout(()  => {
                expect(mockAxios.history.post.length).to.eq(1);
                expect(mockAxios.history.post[0].url).to.eq("/reports/name1/run/");

                //should also hide modal
                expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

                done();
            });

        });

        it('does not post run request when cancel run button is pressed', (done) => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.setData({
                showModal: true,
                pollingTimer: null,
                runningKey: "",
                runningStatus: "",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            });

            wrapper.find("#cancel-run-btn").trigger("click");

            setTimeout(()  => {
                expect(mockAxios.history.post.length).to.eq(0);

                //should also hide modal
                expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

                done();
            });
        });

        it('updates status when run request fails', (done) => {
            const wrapper = mount(RunReport, runReportProps);

            mockAxios.onPost('/reports/name1/run/')
                .reply(500);

            wrapper.setData({
                showModal: true,
                pollingTimer: null,
                runningKey: "",
                runningStatus: "",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            });

            wrapper.find("#confirm-run-btn").trigger("click");

            setTimeout(()  => {
                expect(wrapper.find('#run-report-status').text()).to.contain("Running status: Error when running report");

                //should also hide modal
                expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

                done();
            });

        });

        it('updates status and starts polling when run request is successful', (done) => {
            const wrapper = mount(RunReport, runReportProps);

            mockAxios.onPost('/reports/name1/run/')
                .reply(200, {"data": {"key": "some_key"}});

            wrapper.setData({
                showModal: true,
                pollingTimer: null,
                runningKey: "",
                runningStatus: "",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            });

            wrapper.find("#confirm-run-btn").trigger("click");

            setTimeout(()  => {
                expect(wrapper.find('#run-report-status').text()).to.contain("Running status: Run started");

                //expect key to have been set and polling timer to have been created
                expect(wrapper.vm.$data["runningKey"]).to.eq("some_key");
                expect(wrapper.vm.$data["pollingTimer"]).to.not.eq(null);

                //should also hide modal
                expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

                done();
            });
        });

        it('clears status and stops polling when dismiss clicked', () => {
            const wrapper = mount(RunReport, runReportProps);

            wrapper.setData({
                showModal: false,
                pollingTimer: setTimeout(()=>{}),
                runningKey: "some_key",
                runningStatus: "running_status",
                newVersionFromRun: "new_version",
                newVersionDisplayName: "A new version"
            });

            wrapper.find("#run-report-dismiss").trigger("click");

            expect(wrapper.find("#run-report-status").exists()).to.eq(false);
            expect(wrapper.vm.$data["pollingTimer"]).to.eq(null);

        });

    });

});
