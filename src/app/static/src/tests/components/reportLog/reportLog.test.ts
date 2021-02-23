import Vue from "vue";
import {mount, shallowMount} from "@vue/test-utils";
import ReportLog from "../../../js/components/reportLog/reportLog.vue";
import ReportList from "../../../js/components/runReport/reportList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";
import ParameterList from "../../../js/components/runReport/parameterList.vue";

describe("runReport", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/running/')
            .reply(200, {"data": reports});
    });

    const reports = [
        {name: "report1", date: new Date().toISOString(), key: 'key1'},
        {name: "report2", date: null, key: 'key2'}
    ];

    const initialData = {
        data() {
            return {
                reports: reports,
                logsRefreshing: false
            }
        }
    }

    const getWrapper = (report = reports, data = initialData) => {
        mockAxios.onGet('http://app/running/')
            .reply(200, {"data": report});
        return mount(ReportLog, data);
    }

    it("renders reportLog", async () => {
        // mockAxios.onGet('http://app/running/')
        //     .reply(200, {"data": reports});
        const wrapper = shallowMount(ReportLog);

        await Vue.nextTick()
        await Vue.nextTick()
        await Vue.nextTick()
        
        // const button = wrapper.find("button")
        // button.trigger('click')

        // await Vue.nextTick()

        // expect(wrapper.text()).toBe('true');
        expect(wrapper.find("#logs-form-group").exists()).toBe(true);
        // const options = wrapper.findAll("#git-branch-form-group select option");
        // expect(options.length).toBe(2);
        // expect(options.at(0).text()).toBe("master");
        // expect(options.at(0).attributes().value).toBe("master");
        // expect(options.at(1).text()).toBe("dev");
        // expect(options.at(1).attributes().value).toBe("dev");

        // setTimeout(() => {
        //     expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
        //     const commitOptions = wrapper.findAll("#git-commit option");
        //     expect(commitOptions.length).toBe(2);
        //     expect(commitOptions.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
        //     expect(commitOptions.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

        //     expect(wrapper.vm.$data.selectedCommitId).toBe("abcdef");
            // done();
        // })
    });
});
