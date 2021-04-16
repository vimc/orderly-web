import {mount, shallowMount} from "@vue/test-utils";
import ReportLog from "../../../js/components/reportLog/reportLog.vue";
import {mockAxios} from "../../mockAxios";
import runningReportDetails from "../../../js/components/reportLog/runningReportDetails.vue";

describe("runReport", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/reports/running/')
            .reply(200, {"data": reports});
    });

    const reports = [
        {name: "report1", date: new Date().toISOString(), key: 'key1'},
        {name: "report2", date: new Date().toISOString(), key: 'key2'}
    ];

    const initialData = {
        data() {
            return {
                reports: [],
                reportLogsEnabled: true,
                logsRefreshing: false,
                selectedReport: "",
                error: "",
                defaultMessage: "",
                selectedRunningReportKey: "fakeKey"
            }
        }
    }

    const getWrapper = (report = reports, data = initialData) => {
        mockAxios.onGet('http://app/reports/running/')
            .reply(200, {"data": report});
        return mount(ReportLog, data);
    }

    it("renders reportLog", async (done) => {
        const wrapper = shallowMount(ReportLog);

        expect(wrapper.find("h2").text()).toBe("Running report logs");
        expect(wrapper.find("#noReportsRun").text()).toBe("No reports have been run yet");
        expect(wrapper.find("#logs-form-group").exists()).toBe(false);
        expect(wrapper.findComponent(runningReportDetails).exists()).toBe(false)

        setTimeout(() => {
            expect(wrapper.find("#logs-form-group").exists()).toBe(true);
            expect(wrapper.find("#noReportsRun").exists()).toBe(false);
            expect(wrapper.find("label").text()).toBe("Show logs for");
            expect(wrapper.find("running-reports-list-stub").props("reports")).toEqual(reports);
            expect(wrapper.find("error-info-stub").props("apiError")).toEqual("");
            expect(wrapper.find("error-info-stub").props("defaultMessage")).toEqual("");
            done();
        })
    });

    it("show error message if error getting reports running", (done) => {
        mockAxios.onGet('http://app/reports/running/')
            .reply(500, "TEST ERROR");
        const wrapper = shallowMount(ReportLog);

        setTimeout(() => {
            expect(wrapper.find("error-info-stub").props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.find("error-info-stub").props("defaultMessage")).toBe("An error occurred fetching the running reports");
            done();
        })
    });

    it(`renders report details component correctly`, async () => {
        const wrapper = shallowMount(ReportLog, {
            propsData: {
                selectedRunningReportKey: "key1"
            },
            data() {
                return {
                    reports: reports
                }
            }
        });
        expect(wrapper.findComponent(runningReportDetails).exists()).toBe(true)
        expect(wrapper.findComponent(runningReportDetails).props("reportKey")).toBe("key1")
    })
});
