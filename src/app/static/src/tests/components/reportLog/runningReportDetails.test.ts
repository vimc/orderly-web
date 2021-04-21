import {shallowMount} from "@vue/test-utils"
import runningReportsDetails from "../../../js/components/reportLog/runningReportDetails.vue"
import {mockAxios} from "../../mockAxios"
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {longTimestamp} from "../../../js/utils/helpers";


describe(`runningReportDetails`, () => {

    const props = {
        reportKey: "half_aardwolf"
    }

    const initialReportLog = {
        email: "test@example.com",
        date: new Date(2021, 3, 21, 9, 26, 54).toISOString(),
        report: "minimal",
        instances: { "database": "support", "instance" : "annexe"},
        params: {"name" : "nmin", "cologne" : "ey6"},
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

    it("displays report name and date as expected", () => {
        const wrapper = getWrapper();

        const name = wrapper.find("#report-name");
        expect(name.findAll("span").at(0).text()).toBe("Report:");
        expect(name.findAll("span").at(1).text()).toBe("minimal");

        const start = wrapper.find("#report-start");
        expect(start.findAll("span").at(0).text()).toBe("Run started:");
        expect(start.findAll("span").at(1).text()).toBe("Wed Apr 21 2021, 09:26");
    });


    it(`displays git branch data as expected`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#report-log").find("#report-git-branch").exists()).toBeTruthy()
        const spans = wrapper.find("#report-git-branch").findAll("span")
        expect(spans.at(0).text()).toBe("Git branch:")
        expect(spans.at(1).text()).toBe("branch value")
    })

    it(`displays git commit data as expected`,  () => {
            const wrapper = getWrapper()
            expect(wrapper.find("#report-log").find("#report-git-commit").exists()).toBeTruthy()
            const spans = wrapper.find("#report-git-commit").findAll("span")
            expect(spans.at(0).text()).toBe("Git commit:")
            expect(spans.at(1).text()).toBe("commit value")
    })

    it(`displays parameter data as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-params").findAll("span")
            expect(spans.at(0).text()).toBe("Parameters:")

            const divs = spans.at(1).findAll("div")
            const keyValSpan1 = divs.at(0).findAll("span")
            expect(keyValSpan1.at(0).text()).toBe("name")
            expect(keyValSpan1.at(1).text()).toBe("nmin")

            const keyValSpan2 = divs.at(1).findAll("span")
            expect(keyValSpan2.at(0).text()).toBe("cologne")
            expect(keyValSpan2.at(1).text()).toBe("ey6")
    })

    it(`displays instance data values as expected`,  () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-database-source").findAll("span")
            expect(spans.at(0).text()).toBe("Database:")

            const liValues = spans.at(1).findAll("ul li")
            expect(liValues.at(0).text()).toBe("support")
            expect(liValues.at(1).text()).toBe("annexe")
    })

    it(`displays instance data keys as expected`, () => {
            const wrapper = getWrapper()
            const spans = wrapper.find("#report-database-instance").findAll("span")
            expect(spans.at(0).text()).toBe("Instance:")

            const liKeys = spans.at(1).findAll("ul li")
            expect(liKeys.at(0).text()).toBe("database")
            expect(liKeys.at(1).text()).toBe("instance")
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
        const getWrapper = () => {
            return shallowMount(runningReportsDetails,
                {
                    propsData: {
                        reportKey: key
                    }
                })
        }
        const wrapper = getWrapper()

        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(200, {"data": initialReportLog});
        setTimeout(() => {
            expect(wrapper.find("#no-logs").exists()).toBe(true)
            expect(wrapper.find("#no-logs").text()).toBe("There are no logs to display")
            done()
        })
    })

    it(`it displays error message when report key in not valid`, async(done) => {
        const key = "fakeKey"
        const getWrapper = () => {
            return shallowMount(runningReportsDetails,
                {
                    propsData: {
                        reportKey: key
                    }
                })
        }
        const wrapper = getWrapper()
        mockAxios.onGet(`http://app/running/${key}/logs/`)
            .reply(500, "Error");

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("Error")
            expect(wrapper.find(ErrorInfo).props("defaultMessage"))
                .toBe("An error occurred when fetching logs")
            done()
        })
    })
})
