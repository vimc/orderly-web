import {shallowMount} from "@vue/test-utils"
import runningReportsDetails from "../../../js/components/runReportTabs/runningReportsDetails.vue"
import {mockAxios} from "../../mockAxios"
import ErrorInfo from "../../../js/components/errorInfo.vue";


describe(`runningReportsDetails`, () => {

    const props = {
        reportKey: "half_aardwolf"
    }
    const initialReportLog = {
        email: "test@example.com",
        date: "",
        report: "minimal",
        instances: {
            "0": {"source": "support", "annexe": "annexe val"},
            "1": {"source": "assist", "annexe": "annexe 2"}
        },
        params: {
            "0": {"name": "nmin", "value": "ey6"},
            "1": {"name": "cologne", "value": "mfk"}
        },
        git_branch: "branch value",
        git_commit: "commit value",
        status: "complete",
        logs: "some logs",
        report_version: "version"
    }

    const getWrapper = (propsData = props, reportLog = initialReportLog) => {
        return shallowMount(runningReportsDetails,
            {
                propsData,
                data() {
                    return {
                        reportLog: reportLog
                    }
                }
            })
    }


    it(`displays git branch data as expected`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#report-log").find("#report-git-branch").exists()).toBeTruthy()
        const spans = wrapper.find("#report-git-branch").findAll("span")
        expect(spans.at(0).text()).toBe("Github branch:")
        expect(spans.at(1).text()).toBe("branch value")
    })

    it(`displays git commit data as expected`,  () => {
            const wrapper = getWrapper()
            expect(wrapper.find("#report-log").find("#report-git-commit").exists()).toBeTruthy()
            const spans = wrapper.find("#report-git-commit").findAll("span")
            expect(spans.at(0).text()).toBe("Github commit:")
            expect(spans.at(1).text()).toBe("commit value")
    })

    it(`displays parameter data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-params").findAll("span")
            expect(spans.at(0).text()).toBe("Parameters:")

            const divs = spans.at(1).findAll("div")
            const valueSpan1 = divs.at(0).findAll("span")
            expect(valueSpan1.at(0).text()).toBe("nmin:")
            expect(valueSpan1.at(1).text()).toBe("ey6")

            const valueSpan2 = divs.at(1).findAll("span")
            expect(valueSpan2.at(0).text()).toBe("cologne:")
            expect(valueSpan2.at(1).text()).toBe("mfk")
    })

    it(`displays Database(source) data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-database-source").findAll("span")
            expect(spans.at(0).text()).toBe("Database(source):")

            const liValues = spans.at(1).findAll("ul li")
            expect(liValues.at(0).text()).toBe("support")
            expect(liValues.at(1).text()).toBe("assist")
    })

    it(`displays Database(annexe) data as expected`, () => {

            const wrapper = getWrapper()
            const spans = wrapper.find("#report-database-instance").findAll("span")
            expect(spans.at(0).text()).toBe("Database(annexe):")

            const liValues = spans.at(1).findAll("ul li")
            expect(liValues.at(0).text()).toBe("annexe val")
            expect(liValues.at(1).text()).toBe("annexe 2")
    })

    it(`displays status data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-status").findAll("span")
            expect(spans.at(0).text()).toBe("Status:")
            expect(spans.at(1).text()).toBe("complete")
    })

    it(`displays version data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-version").findAll("span")
            expect(spans.at(0).text()).toBe("Report version:")
            expect(spans.at(1).text()).toBe("version")
    })

    it(`displays Logs data as expected`,  () => {
            const wrapper = getWrapper()
            const textArea = wrapper.find("#report-logs").find("textarea")
            expect(textArea.text()).toBe("some logs")
    })

    it(`does not displays data when report key in not given`, async (done) => {
        const key = ""
        const mockInitialReportLog = {
            email: "",
            date: "",
            report: "",
            instances: {
                "0": {"source": "", "annexe": ""},
                "1": {"source": "", "annexe": ""}
            },
            params: {
                "0": {"name": "", "value": ""},
                "1": {"name": "", "value": ""}
            },
            git_branch: "",
            git_commit: "",
            status: "",
            logs: "",
            report_version: ""
        }
        const wrapper = getWrapper({reportKey: key}, mockInitialReportLog)

        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": initialReportLog});
        setTimeout(() => {
            expect(wrapper.find("#report-logs").exists()).toBe(false)
            done()
        })
    })

    it(`it displays error message when report key in not valid`, async(done) => {
        const key = "fakeKey"
        const mockInitialReportLog = {
            email: "",
            date: "",
            report: "",
            instances: {
                "0": {"source": "", "annexe": ""},
                "1": {"source": "", "annexe": ""}
            },
            params: {
                "0": {"name": "", "value": ""},
                "1": {"name": "", "value": ""}
            },
            git_branch: "",
            git_commit: "",
            status: "",
            logs: "",
            report_version: ""
        }
        const wrapper = getWrapper({reportKey: key}, mockInitialReportLog)
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(500, "Error");

        setTimeout(() => {
            expect(wrapper.find("#report-logs").exists()).toBe(false)
            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("Error")
            expect(wrapper.find(ErrorInfo).props("defaultMessage"))
                .toBe("An error occurred when fetching metadata")
            done()
        })
    })
})