import {expect} from "chai";
import {describe} from "mocha";
import * as sinon from "sinon";
import {session} from "../js/utils/session";

describe('session', () => {

    //sessionStorage is undefined when running these tests
    const fakeStorage = {
        getItem: function(key) {
            return "value for " + key;
        },
        setItem: function(key, value){},
        removeItem: function(key){}
    };

    it('gets running report status from local storage', () => {
        Object.defineProperty(window, 'sessionStorage', {
            value: fakeStorage
        });

        const result = session.getRunningReportStatus("report1");

        expect(result.runningStatus).to.eq("value for runningReportStatus_report1_runningStatus");
        expect(result.runningKey).to.eq("value for runningReportStatus_report1_runningKey");
        expect(result.newVersionFromRun).to.eq("value for runningReportStatus_report1_newVersionFromRun");
    });

    it('sets running report status in local storage', () => {
        Object.defineProperty(window, 'sessionStorage', {
            value: fakeStorage
        });

        const spySetStorage = sinon.spy(fakeStorage, "setItem");

        const testStatus = {
            runningStatus: "still going",
            runningKey: "bewildered_mongoose",
            newVersionFromRun: "v1",
            newVersionDisplayName: "Version One"
        };

        session.setRunningReportStatus("report1", testStatus);

        expect(spySetStorage.getCall(0).args[0]).to.eql("runningReportStatus_report1_runningStatus");
        expect(spySetStorage.getCall(0).args[1]).to.eql("still going");

        expect(spySetStorage.getCall(1).args[0]).to.eql("runningReportStatus_report1_runningKey");
        expect(spySetStorage.getCall(1).args[1]).to.eql("bewildered_mongoose");

        expect(spySetStorage.getCall(2).args[0]).to.eql("runningReportStatus_report1_newVersionFromRun");
        expect(spySetStorage.getCall(2).args[1]).to.eql("v1");

    });

    it('removes running report status from local storage', () => {
        Object.defineProperty(window, 'sessionStorage', {
            value: fakeStorage
        });

        const spyRemoveStorage = sinon.spy(fakeStorage, "removeItem");

        session.removeRunningReportStatus("report1");

        expect(spyRemoveStorage.getCall(0).args[0]).to.eql("runningReportStatus_report1_runningStatus");

        expect(spyRemoveStorage.getCall(1).args[0]).to.eql("runningReportStatus_report1_runningKey");

        expect(spyRemoveStorage.getCall(2).args[0]).to.eql("runningReportStatus_report1_newVersionFromRun");

    });

});