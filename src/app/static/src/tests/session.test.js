import {
    SELECTED_RUNNING_REPORT_KEY,
    SELECTED_RUNNING_REPORT_TAB,
    SELECTED_RUNNING_WORKFLOW_TAB,
    session
} from "../js/utils/session";

describe('session', () => {

    beforeEach(() => {
        jest.restoreAllMocks()
    });

    it('gets selected tab from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => `value for ${x}`);

        const result = session.getSelectedTab(SELECTED_RUNNING_REPORT_TAB);
        expect(result).toBe("value for selectedRunningReportTab");

        const workflowTab = session.getSelectedTab(SELECTED_RUNNING_WORKFLOW_TAB);
        expect(workflowTab).toBe("value for selectedRunningWorkflowTab");
    });

    it('sets selected tab in local storage', () => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;

        session.setSelectedTab(SELECTED_RUNNING_REPORT_TAB, "reportLogs");
        session.setSelectedTab(SELECTED_RUNNING_WORKFLOW_TAB, "workflowProgress");

        expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportTab");
        expect(spySetStorage.calls[0][1]).toBe("reportLogs");
        expect(spySetStorage.calls[1][0]).toBe("selectedRunningWorkflowTab");
        expect(spySetStorage.calls[1][1]).toBe("workflowProgress");

    });

    it('gets selected running report key from local storage', () => {

        Storage.prototype.getItem = jest.fn((x) => `value for ${x}`);

        const result = session.getSelectedKey(SELECTED_RUNNING_REPORT_KEY);

        expect(result).toBe("value for selectedRunningReportKey");
    });

    it('sets selected running report key in local storage', () => {
        Storage.prototype.setItem = jest.fn();
        const spySetStorage = jest.spyOn(Storage.prototype, 'setItem').mock;

        session.setSelectedKey(SELECTED_RUNNING_REPORT_KEY, "crazypanda");

        expect(spySetStorage.calls[0][0]).toBe("selectedRunningReportKey");
        expect(spySetStorage.calls[0][1]).toBe("crazypanda");

    });
});