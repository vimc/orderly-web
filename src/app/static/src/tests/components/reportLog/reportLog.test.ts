import Vue from "vue";
import {mount, shallowMount} from "@vue/test-utils";
import ReportLog from "../../../js/components/reportLog/reportLog.vue";
import RunningReportsList from "../../../js/components/reportLog/runningReportsList.vue";
import {mockAxios} from "../../mockAxios";

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
                // selectedRunningReportKey: "",
                selectedReport: "",
                error: "",
                defaultMessage: ""
            }
        }
        // props: {
        //     selectedRunningReportKey: ''
        // }
    }

    const initialProps = {
        selectedRunningReportKey: ''
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

    it("displays report list in order and allows selection", (done) => {
        const wrapper = getWrapper();
        wrapper.setProps({selectedRunningReportKey: ''})

        setTimeout(async () => {
            wrapper.find(RunningReportsList).find("a:last-of-type").trigger("click");
            expect(wrapper.vm.$props["selectedRunningReportKey"]).toBe("key1");
            done();
        });
    });

    // it("setting key prop selects appropriated element in list", (done) => {
    //     const wrapper = getWrapper();
    //     wrapper.setProps({selectedRunningReportKey: 'key2'})

    //     setTimeout(async () => {
    //         // const 
    //         expect(wrapper.find(RunningReportsList).html()).toBe("key1");
    //         expect(wrapper.find(RunningReportsList).findAll(".listOption").length).toBe("key1");
    //         // wrapper.find(RunningReportsList).find("a:last-of-type").trigger("click");
    //         // expect(wrapper.vm.$props["selectedRunningReportKey"]).toBe("key1");
    //         done();
    //     });
    // });
});
