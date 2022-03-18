import {mockRunReportRootState} from "../mocks";
import {mutations, RunReportMutation} from "../../js/store/runReport/mutations";

describe("Run report root mutations", () => {

    it("switches tab", () => {
        const state = mockRunReportRootState({
            selectedTab: "RunReport"
        });
        mutations[RunReportMutation.SwitchTab](
            state,
            "ReportLogs")
        expect(state.selectedTab).toBe("ReportLogs");
    })
});
