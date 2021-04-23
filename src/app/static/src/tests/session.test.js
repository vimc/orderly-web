import {session} from "../js/utils/session";

describe('session', () => {

    beforeEach(() => {
        jest.restoreAllMocks()
    });

    it('gets selected tab from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => x);

        const result = session.getSelectedTab();

        expect(result).toBe("runReport");
    });

    it('sets selected tab in local storage', () => {
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;


        session.setSelectedTab("reportLogs");

        expect(spySetStorage.calls[0][0]).toBe("runReport");
        expect(spySetStorage.calls[0][1]).toBe("reportLogs");

    });

    it('gets selected running report key from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => x);

        const result = session.getSelectedRunningReportKey();

        expect(result).toBe("");
    });

    it('sets selected running report key in local storage', () => {
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;


        session.setSelectedRunningReportKey("crazypanda");

        expect(spySetStorage.calls[0][0]).toBe("");
        expect(spySetStorage.calls[0][1]).toBe("crazypanda");

    });

    it('removes selected running report key from local storage', () => {
        const spyRemoveStorage = jest.spyOn(Storage.prototype, 'removeItem').mock;

        session.removeSelectedRunningReportKey();
        expect(spyRemoveStorage.calls[0][0]).toBe("");
    });
});