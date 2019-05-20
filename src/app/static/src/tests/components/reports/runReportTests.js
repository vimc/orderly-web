import {expect} from "chai";
import {describe} from "mocha";
import {mount} from '@vue/test-utils';
import {mockAxios} from "../../mockAxios";
import * as sinon from "sinon";
import RunReport from "../../../js/components/reports/runReport.vue"
import {session} from "../../../js/session";

describe("runReport", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    const runReportProps = {
        propsData: {
            report: {
                name: "name1",
            }
        }
    };

    var sessionStubGetRunningReportStatus;
    var sessionStubSetRunningReportStatus;
    var sessionStubRemoveRunningReportStatus;

    beforeEach(() => {
        sessionStubGetRunningReportStatus = sinon.stub(session, "getRunningReportStatus").callsFake(() => {
                return {
                    runningStatus: null,
                    runningKey: null,
                    newVersionFromRun: null
                }
            }
        );
        sessionStubSetRunningReportStatus = sinon.stub(session, "setRunningReportStatus");
        sessionStubRemoveRunningReportStatus = sinon.stub(session, "removeRunningReportStatus");
    });

    afterEach(() => {
        session.getRunningReportStatus.restore();
        session.setRunningReportStatus.restore();
        session.removeRunningReportStatus.restore();
    });

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
            newVersionFromRun: ""
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
            newVersionFromRun: "20190514-160954-fc295f38"
        });

        expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");
        expect(wrapper.find("#run-report-status").text()).to.contain("Running status: some_status");

        expect(wrapper.find("#run-report-new-version").text()).to.eq("New version: Tue May 14 2019, 16:09");
        expect(wrapper.find("#run-report-new-version a").attributes("href")).to.eq("/reports/name1/20190514-160954-fc295f38");
        expect(wrapper.find("#run-report-dismiss").text()).to.eq("Dismiss");
    });

    it('shows modal', () => {
        const wrapper = mount(RunReport, runReportProps);

        wrapper.setData({
            showModal: true
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
            showModal: true
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
            showModal: true
        });

        wrapper.find("#cancel-run-btn").trigger("click");

        setTimeout(()  => {
            expect(mockAxios.history.post.length).to.eq(0);

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

            done();
        });
    });

    it('updates status and stops polling when run request fails', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        mockAxios.onPost('/reports/name1/run/')
            .reply(500);

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).to.contain("Running status: Error when running report");
            expect(wrapper.vm.$data["pollingTimer"]).to.eq(null);

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(0).args[0]).eq("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(0).args[1];
            expect(storedStatus.runningKey).to.eq("");
            expect(storedStatus.runningStatus).to.eq("Error when running report");
            expect(storedStatus.newVersionFromRun).to.eq(null);

            done();
        });
    });

    it('updates status and starts polling when run request is successful', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        mockAxios.onPost('/reports/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).to.contain("Running status: Run started");

            //expect key to have been set and polling timer to have been created
            expect(wrapper.vm.$data["runningKey"]).to.eq("some_key");
            expect(wrapper.vm.$data["pollingTimer"]).to.not.eq(null);

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(0).args[0]).eq("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(0).args[1];
            expect(storedStatus.runningKey).to.eq("some_key");
            expect(storedStatus.runningStatus).to.eq("Run started");

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
            newVersionFromRun: "20190514-160954-fc295f38"
        });

        wrapper.find("#run-report-dismiss").trigger("click");

        expect(wrapper.find("#run-report-status").exists()).to.eq(false);
        expect(wrapper.vm.$data["pollingTimer"]).to.eq(null);

        //Also expect status in session to have been cleared
        expect(sessionStubRemoveRunningReportStatus.getCall(0).args[0]).eq("name1");
    });

    it('updates status and stops polling when run update request returns success status', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('/reports/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('/reports/some_key/status/')
            .reply(200, {"data": {"status": "success", "version": "20190514-160954-fc295f38"}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).to.contain("Running status: success");
            expect(wrapper.find('#run-report-new-version a').text()).to.eq("Tue May 14 2019, 16:09");
            expect(wrapper.find('#run-report-new-version a').attributes("href")).to.eq("/reports/name1/20190514-160954-fc295f38");

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).to.eq(null);

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).eq("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).to.eq("some_key");
            expect(storedStatus.runningStatus).to.eq("success");
            expect(storedStatus.newVersionFromRun).to.eq("20190514-160954-fc295f38");

            done();
        }, 1800);
    });

    it('updates status and stops polling when run update request returns error status', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('/reports/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('/reports/some_key/status/')
            .reply(200, {"data": {"status": "error", "version": ""}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).to.contain("Running status: error");
            expect(wrapper.find('#run-report-new-version').exists()).to.eq(false);

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).to.eq(null);

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).eq("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).to.eq("some_key");
            expect(storedStatus.runningStatus).to.eq("error");
            expect(storedStatus.newVersionFromRun).to.eq("");

            done();
        }, 1800);

    });

    it('updates status and continues polling when run update request returns non-complete status', (done) => {
       //ie any status other than 'success' or 'error'
        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('/reports/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('/reports/some_key/status/')
            .reply(200, {"data": {"status": "still going", "version": ""}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).to.contain("Running status: still going");
            expect(wrapper.find('#run-report-new-version').exists()).to.eq(false);

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).to.not.eq(null);

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).eq("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).to.eq("some_key");
            expect(storedStatus.runningStatus).to.eq("still going");
            expect(storedStatus.newVersionFromRun).to.eq("");

            done();
        }, 1800);
    });

    it('updates status and stops polling when run update request fails', (done) => {

        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('/reports/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('/reports/some_key/status/')
            .reply(500);

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).to.contain("Running status: Error when fetching report status");
            expect(wrapper.find('#run-report-new-version').exists()).to.eq(false);

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).to.eq(null);

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).eq("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).to.eq("some_key");
            expect(storedStatus.runningStatus).to.eq("Error when fetching report status");
            expect(storedStatus.newVersionFromRun).to.eq("");
            expect(storedStatus.newVersionFromRun).to.eq("");

            done();
        }, 1800);
    });

    it('initialises data from session storage', () => {
        session.getRunningReportStatus.restore();
        sinon.stub(session, "getRunningReportStatus").callsFake(() => {
                return {
                    runningStatus: "storedStatus",
                    runningKey: "storedKey",
                    newVersionFromRun: "20190514-160954-fc295f38"
                }
            }
        );

        const wrapper = mount(RunReport, runReportProps);

        expect(wrapper.find('button[type="submit"]').text()).to.eq("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).to.contain("modal-hide");
        expect(wrapper.find("#run-report-status").text()).to.contain("Running status: storedStatus");

        expect(wrapper.find("#run-report-new-version").text()).to.eq("New version: Tue May 14 2019, 16:09");
        expect(wrapper.find("#run-report-new-version a").attributes("href")).to.eq("/reports/name1/20190514-160954-fc295f38");
        expect(wrapper.find("#run-report-dismiss").text()).to.eq("Dismiss");

        expect(wrapper.vm.$data["runningKey"]).to.eq("storedKey");
        expect(wrapper.vm.$data["pollingTimer"]).to.not.eq(null);

    });

});


