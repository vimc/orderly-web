import {mount} from "@vue/test-utils";
import runReportTab from "../../../js/components/runReportTabs/runReportTabs.vue"
import runningReportsDetails from "../../../js/components/runReportTabs/runningReportsDetails.vue";

describe(`runReportTab`, () => {
    const getWrapper = (reportKey = "", propsData = {}) => {
        return mount(runReportTab,
            {
                propsData,
                data() {
                    return {
                        reportKey: reportKey
                    }
                }
            })
    }

    it(`renders running report tab component correctly`, () => {
        const key = "fakeKey"
        const wrapper = getWrapper(key)
        expect(wrapper.findComponent(runningReportsDetails).exists()).toBe(true)
        expect(wrapper.findComponent(runningReportsDetails).props("reportKey")).toBe("fakeKey")
    })
})