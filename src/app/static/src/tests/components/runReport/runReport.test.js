;import {shallowMount, mount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";
import ErrorInfo from "../../../js/components/errorInfo";
import {mockAxios} from "../../mockAxios";

describe("reportTags", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    const gitBranches = ["master", "dev"];

    it("renders git branch drop down if git supported", () => {
        const wrapper = shallowMount(RunReport, {propsData: {
                metadata: {git_supported: true},
                gitBranches
            }});

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);

        //should not render commits yet
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);

        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(3);
        expect(options.at(0).text()).toBe("-- Select a branch --");
        expect(options.at(0).attributes().disabled).toBe("disabled");
        expect(options.at(1).text()).toBe("master");
        expect(options.at(1).attributes().value).toBe("master");
        expect(options.at(2).text()).toBe("dev");
        expect(options.at(2).attributes().value).toBe("dev");
    });

    it("does not render git drop downs if git not supported", () => {
        const wrapper = shallowMount(RunReport, {propsData: {
                metadata: {git_supported: false},
                gitBranches: null
            }});

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });

    it("calls api to get commits when branch changes and updates commits drop down", async (done) => {
        const wrapper = mount(RunReport, {propsData: {
                metadata: {git_supported: true},
                gitBranches
            }});

        const mockCommits = [
            { id: "abcdef", display_date_time: "Mon Jun 08, 12:01" },
            { id: "abc123", display_date_time: "Tue Jun 09, 13:11" }
        ];

        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(200, {"data": mockCommits});

        wrapper.findAll("#git-branch option").at(1).setSelected();

        expect(wrapper.vm.selectedBranch).toBe("master");

        setTimeout(() => {
            expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
            const options = wrapper.findAll("#git-commit option");
            expect(options.length).toBe(2);
            expect(options.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
            expect(options.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

            expect(wrapper.vm.selectedCommitId).toBe("abcdef");

            expect(wrapper.vm.error).toBe("");
            expect(wrapper.vm.defaultMessage).toBe("");

            done();
        });
    });

    it("show error message if error getting git commits", async (done) => {
        const wrapper = shallowMount(RunReport, {propsData: {
                metadata: {git_supported: true},
                gitBranches
            }});

        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(500, "TEST ERROR");

        wrapper.findAll("#git-branch option").at(1).setSelected();

        setTimeout(() => {
            expect(wrapper.vm.error.response.data).toBe("TEST ERROR");
            expect(wrapper.vm.defaultMessage).toBe("An error occurred fetching Git commits");

            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching Git commits");

            done();
        });

    });
});