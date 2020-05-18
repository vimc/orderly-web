import {shallowMount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";

describe("reportTags", () => {
    it("renders git branch drop down if git supported", () => {
        const wrapper = shallowMount(RunReport, {propsData: {
                metadata: {git_supported: true},
                gitBranches: ["master", "dev"]
            }});

        expect(wrapper.find("#gitBranchFormGroup").exists()).toBe(true);

        const options = wrapper.findAll("#gitBranchFormGroup select option");
        expect(options.length).toBe(3);
        expect(options.at(0).text()).toBe("-- Select a branch --");
        expect(options.at(0).attributes().disabled).toBe("disabled");
        expect(options.at(1).text()).toBe("master");
        expect(options.at(1).attributes().value).toBe("master");
        expect(options.at(2).text()).toBe("dev");
        expect(options.at(2).attributes().value).toBe("dev");
    });

    it("does not render git branch drop down if git not supported", () => {
        const wrapper = shallowMount(RunReport, {propsData: {
                metadata: {git_supported: false},
                gitBranches: null
            }});

        expect(wrapper.find("#gitBranchFormGroup").exists()).toBe(false);
    });
});