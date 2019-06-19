import {mount} from '@vue/test-utils';
import {mockAxios} from "../../mockAxios";
import * as sinon from "sinon";
import RunReport from "../../../js/components/reports/runReport.vue"
import {session} from "../../../js/utils/session";

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

        expect(wrapper.find('button[type="submit"]').text()).toBe("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");
        expect(wrapper.find("#run-report-status").exists()).toBe(false);
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

        expect(wrapper.find('button[type="submit"]').text()).toBe("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");
        expect(wrapper.find("#run-report-status").text()).toContain("Running status: some_status");

        expect(wrapper.find("#run-report-new-version").exists()).toBe(false);
        expect(wrapper.find("#run-report-dismiss").text()).toBe("Dismiss");
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

        expect(wrapper.find('button[type="submit"]').text()).toBe("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");
        expect(wrapper.find("#run-report-status").text()).toContain("Running status: some_status");

        expect(wrapper.find("#run-report-new-version").text()).toBe("New version: Tue May 14 2019, 16:09");
        expect(wrapper.find("#run-report-new-version a").attributes("href")).toBe("http://app/report/name1/20190514-160954-fc295f38");
        expect(wrapper.find("#run-report-dismiss").text()).toBe("Dismiss");
    });

    it('shows modal', () => {
        const wrapper = mount(RunReport, runReportProps);

        wrapper.setData({
            showModal: true
        });

        expect(wrapper.find('button[type="submit"]').text()).toBe("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-show");
        expect(wrapper.find("#run-report-status").exists()).toBe(false);


    });

    it('displays modal when run button is pressed', () => {
        const wrapper = mount(RunReport, runReportProps);

        wrapper.find('button[type="submit"]').trigger("click");

        expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-show");

    });

    it('posts run request when confirm run button is pressed', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe("http://app/report/name1/run/");

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");

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
            expect(mockAxios.history.post.length).toBe(0);

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");

            done();
        });
    });

    it('updates status and stops polling when run request fails', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        mockAxios.onPost('/report/name1/run/')
            .reply(500);

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).toContain("Running status: Error when running report");
            expect(wrapper.vm.$data["pollingTimer"]).toBeNull();

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(0).args[0]).toBe("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(0).args[1];
            expect(storedStatus.runningKey).toBe("");
            expect(storedStatus.runningStatus).toBe("Error when running report");
            expect(storedStatus.newVersionFromRun).toBe(null);

            done();
        });
    });

    it('updates status and starts polling when run request is successful', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        mockAxios.onPost('http://app/report/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).toContain("Running status: Run started");

            //expect key to have been set and polling timer to have been created
            expect(wrapper.vm.$data["runningKey"]).toBe("some_key");
            expect(wrapper.vm.$data["pollingTimer"]).not.toBeNull();

            //should also hide modal
            expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(0).args[0]).toBe("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(0).args[1];
            expect(storedStatus.runningKey).toBe("some_key");
            expect(storedStatus.runningStatus).toBe("Run started");

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

        expect(wrapper.find("#run-report-status").exists()).toBe(false);
        expect(wrapper.vm.$data["pollingTimer"]).toBeNull();

        //Also expect status in session to have been cleared
        expect(sessionStubRemoveRunningReportStatus.getCall(0).args[0]).toBe("name1");
    });

    it('updates status and stops polling when run update request returns success status', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('http://app/report/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('http://app/report/some_key/status/')
            .reply(200, {"data": {"status": "success", "version": "20190514-160954-fc295f38"}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).toContain("Running status: success");
            expect(wrapper.find('#run-report-new-version a').text()).toBe("Tue May 14 2019, 16:09");
            expect(wrapper.find('#run-report-new-version a').attributes("href")).toBe("http://app/report/name1/20190514-160954-fc295f38");

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).toBeNull();

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).toBe("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).toBe("some_key");
            expect(storedStatus.runningStatus).toBe("success");
            expect(storedStatus.newVersionFromRun).toBe("20190514-160954-fc295f38");

            done();
        }, 1800);
    });

    it('updates status and stops polling when run update request returns error status', (done) => {
        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('http://app/report/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('http://app/report/some_key/status/')
            .reply(200, {"data": {"status": "error", "version": ""}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).toContain("Running status: error");
            expect(wrapper.find('#run-report-new-version').exists()).toBe(false);

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).toBe(null);

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).toBe("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).toBe("some_key");
            expect(storedStatus.runningStatus).toBe("error");
            expect(storedStatus.newVersionFromRun).toBe("");

            done();
        }, 1800);

    });

    it('updates status and continues polling when run update request returns non-complete status', (done) => {
       //ie any status other than 'success' or 'error'
        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('http://app/report/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('http://app/report/some_key/status/')
            .reply(200, {"data": {"status": "still going", "version": ""}});

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).toContain("Running status: still going");
            expect(wrapper.find('#run-report-new-version').exists()).toBe(false);
            expect(wrapper.vm.$data["pollingTimer"]).not.toBeNull();

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).toBe("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).toBe("some_key");
            expect(storedStatus.runningStatus).toBe("still going");
            expect(storedStatus.newVersionFromRun).toBe("");

            done();
        }, 1800);
    });

    it('updates status and stops polling when run update request fails', (done) => {

        const wrapper = mount(RunReport, runReportProps);

        //mock endpoint initially called to run the report
        mockAxios.onPost('http://app/report/name1/run/')
            .reply(200, {"data": {"key": "some_key"}});

        //mock endpoint which is polled
        mockAxios.onGet('http://app/report/some_key/status/')
            .reply(500);

        wrapper.setData({
            showModal: true
        });

        wrapper.find("#confirm-run-btn").trigger("click");

        setTimeout(()  => {
            expect(wrapper.find('#run-report-status').text()).toContain("Running status: Error when fetching report status");
            expect(wrapper.find('#run-report-new-version').exists()).toBe(false);

            //expect key to have been set and polling timer to have been cleared
            expect(wrapper.vm.$data["pollingTimer"]).toBeNull();

            //expect status in session to have been set
            expect(sessionStubSetRunningReportStatus.getCall(1).args[0]).toBe("name1");
            const storedStatus = sessionStubSetRunningReportStatus.getCall(1).args[1];
            expect(storedStatus.runningKey).toBe("some_key");
            expect(storedStatus.runningStatus).toBe("Error when fetching report status");
            expect(storedStatus.newVersionFromRun).toBe("");
            expect(storedStatus.newVersionFromRun).toBe("");

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

        expect(wrapper.find('button[type="submit"]').text()).toBe("Run report");
        expect(wrapper.find('#run-report-confirm').classes()).toContain("modal-hide");
        expect(wrapper.find("#run-report-status").text()).toContain("Running status: storedStatus");

        expect(wrapper.find("#run-report-new-version").text()).toBe("New version: Tue May 14 2019, 16:09");
        expect(wrapper.find("#run-report-new-version a").attributes("href")).toBe("http://app/report/name1/20190514-160954-fc295f38");
        expect(wrapper.find("#run-report-dismiss").text()).toBe("Dismiss");

        expect(wrapper.vm.$data["runningKey"]).toBe("storedKey");
        expect(wrapper.vm.$data["pollingTimer"]).not.toBeNull();

    });

});


