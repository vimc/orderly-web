import {session} from "../js/utils/session";

describe('session', () => {

    beforeEach(() => {
        jest.restoreAllMocks()
        // Storage.prototype.clear()
    });

    it('gets selected tab from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => x);

        const result = session.getSelectedTab();

        expect(result).toBe("selectedRunningReportTab");
    });

    it('sets selected tab in local storage', () => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;


        session.setSelectedTab("reportLogs");

        expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportTab");
        expect(spySetStorage.calls[0][1]).toBe("reportLogs");

    });

    it('gets selected running report key from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => `value for ${x}`);

        const result = session.getSelectedRunningReportKey();

        expect(result).toBe("value for selectedRunningReportKey");
    });

    it('sets selected running report key in local storage', () => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;

        session.setSelectedRunningReportKey("crazypanda");

        expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportKey");
        expect(spySetStorage.calls[0][1]).toBe("crazypanda");

    });
});