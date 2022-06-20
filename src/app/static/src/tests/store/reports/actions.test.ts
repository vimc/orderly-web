import {mockAxios} from "../../mockAxios";
import {actions} from "../../../js/store/reports/actions";
import {mockActionContext, mockFailure, mockGitState, mockRunReportRootState, mockSuccess} from "../../mocks";
import {ReportsMutation} from "../../../js/store/reports/mutations";

const report2 = [{name: "report2", date: new Date(2021, 3, 21, 9, 10).toISOString()}];

describe("vuex reportList action", () => {
    beforeEach(() => {
        mockAxios.reset();

    });

    it("fetches reports", async() => {
        mockAxios.onGet("http://app/reports/runnable/?branch=master&commit=c3768eb")
            .reply(200, mockSuccess(report2));

        const rootState = mockRunReportRootState(
            {
                git: mockGitState({
                    selectedCommit: "c3768eb",
                    selectedBranch: "master",
                    metadata: {git_supported: true}
                })
            })

        const commit = jest.fn()

        await actions.GetReports(mockActionContext({commit, rootState}), false)
        expect(mockAxios.history.get.length).toBe(1)
        expect(mockAxios.history.get[0].url).toBe( "http://app/reports/runnable/?branch=master&commit=c3768eb")

        expect(commit.mock.calls.length).toBe(1)
        expect(commit.mock.calls[0]).toEqual([{type: ReportsMutation.SetReports, payload: report2}])
    })

    it("does commit error when fetching reports", async() => {
        mockAxios.onGet("http://app/reports/runnable/?branch=master&commit=c3768eb")
            .reply(500, mockFailure("Test Error"));

        const rootState = mockRunReportRootState(
            {
                git: mockGitState({
                    selectedCommit: "c3768eb",
                    selectedBranch: "master",
                    metadata: {git_supported: true}
                })
            })

        const commit = jest.fn()

        await actions.GetReports(mockActionContext({commit, rootState}), false)
        expect(mockAxios.history.get.length).toBe(1)

        expect(commit.mock.calls.length).toBe(1)
        expect(commit.mock.calls[0][0]).toStrictEqual({
            type: ReportsMutation.SetReportsError,
            payload: {
                code: "ERROR",
                message: "Test Error"
            }
        })
    })
})