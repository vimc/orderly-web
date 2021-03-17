import {shallowMount} from '@vue/test-utils';
import runReportInline from "../../../js/components/reports/runReportInline";

describe("runReportInline", () => {
    const getWrapper = () => {
        return shallowMount(runReportInline, {propsData: {report: {name: "report-data"}}})
    }

    it(`it renders div label as expected`, () => {
        const wrapper = getWrapper()
        const reportInLine = wrapper.find("#run-report")
        expect(reportInLine.find("label").text())
            .toBe("Run")

        expect(wrapper.vm.$props.report.name).toBe("report-data")
    })

    it(`it renders divs as expected`, () => {
        const wrapper = getWrapper()
        expect(wrapper.findAll("div").at(1).text())
            .toBe("Run this report to create a new version.")

        expect(wrapper.find("div a").text()).toBe("Run report")
    })

    it(`it renders href link as expected`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("div a").attributes("href")).toBe("http://app/run-report?report-name=report-data")
    })
});


