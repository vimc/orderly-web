import {mutations, ReportsMutation} from "../../../js/store/reports/mutations";
import {mockReportsState} from "../../mocks";

describe("reports mutations", () => {

    const state = mockReportsState()
    const report = {name: "report", date: null}

    it("can set reports", () => {
        mutations[ReportsMutation.FetchReports](state, {payload: [report]})
        expect(state.reports).toStrictEqual([report])
    })

    it("can sort reports", () => {
        const reportList = [
            {name: "ballReport", date: new Date(2021, 3, 21, 9, 4).toISOString()},
            {name: "appleReport", date: new Date(2021, 3, 21, 9, 10).toISOString()},
            {name: "report", date: new Date(2021, 3, 21, 9, 2).toISOString()},
        ];
        mutations[ReportsMutation.FetchReports](state, {payload: reportList})

        expect(state.reports).toEqual([
            {name: "appleReport", date: "2021-04-21T09:10:00.000Z"},
            {name: "ballReport", date: "2021-04-21T09:04:00.000Z"},
            {name: "report", date: "2021-04-21T09:02:00.000Z"}
        ])
    })

    it("can set selectedReport", () => {
        mutations[ReportsMutation.SelectReport](state, report)
        expect(state.selectedReport).toStrictEqual(report)
    })

    it("can set report error", () => {
        mutations[ReportsMutation.FetchReportsError](state, {payload: "Error"})
        expect(state.reportsError).toBe("Error")
    })
})