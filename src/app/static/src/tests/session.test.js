import {session} from "../js/session";

describe('session', () => {

    beforeEach(() => {
        jest.restoreAllMocks()
    });

    it('gets running report status from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => `value for ${x}`);

        const result = session.getRunningReportStatus("report1");

        expect(result.runningStatus).toBe("value for runningReportStatus_report1_runningStatus");
        expect(result.runningKey).toBe("value for runningReportStatus_report1_runningKey");
        expect(result.newVersionFromRun).toBe("value for runningReportStatus_report1_newVersionFromRun");
    });

    it('sets running report status in local storage', () => {
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;

        const testStatus = {
            runningStatus: "still going",
            runningKey: "bewildered_mongoose",
            newVersionFromRun: "v1"
        };

        session.setRunningReportStatus("report1", testStatus);

        expect(spySetStorage.calls[0][0]).toBe("runningReportStatus_report1_runningStatus");
        expect(spySetStorage.calls[0][1]).toBe("still going");

        expect(spySetStorage.calls[1][0]).toBe("runningReportStatus_report1_runningKey");
        expect(spySetStorage.calls[1][1]).toBe("bewildered_mongoose");

        expect(spySetStorage.calls[2][0]).toBe("runningReportStatus_report1_newVersionFromRun");
        expect(spySetStorage.calls[2][1]).toBe("v1");

    });

    it('removes running report status from local storage', () => {

        const spyRemoveStorage = jest.spyOn(Storage.prototype, 'removeItem').mock;

        session.removeRunningReportStatus("report1");

        expect(spyRemoveStorage.calls[0][0]).toBe("runningReportStatus_report1_runningStatus");

        expect(spyRemoveStorage.calls[1][0]).toBe("runningReportStatus_report1_runningKey");

        expect(spyRemoveStorage.calls[2][0]).toBe("runningReportStatus_report1_newVersionFromRun");

    });

    it('removes item from local storage if value is null', () => {

        const spyRemoveStorage = jest.spyOn(Storage.prototype, 'removeItem').mock;
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;

        const testStatus = {
            runningStatus: "error",
            runningKey: "bewildered_mongoose",
            newVersionFromRun: null
        };

        session.setRunningReportStatus("report1", testStatus);

        expect(spySetStorage.calls[0][0]).toBe("runningReportStatus_report1_runningStatus");
        expect(spySetStorage.calls[1][0]).toBe("runningReportStatus_report1_runningKey");
        expect(spyRemoveStorage.calls[0][0]).toBe("runningReportStatus_report1_newVersionFromRun");

    });

});