import Vue from "vue";
import {shallowMount, mount} from "@vue/test-utils";
import RunReport from "../../../js/components/runReport/runReport.vue";
import ReportList from "../../../js/components/runReport/reportList.vue";
import ErrorInfo from "../../../js/components/errorInfo";
import {mockAxios} from "../../mockAxios";

describe("runReport", () => {
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(200, {"data": gitCommits});
        mockAxios.onPost('http://app/git/fetch/')
            .reply(200, {"data": mockFetch});
    });

    const gitCommits = [
        {id: "abcdef", date_time: "Mon Jun 08, 12:01"},
        {id: "abc123", date_time: "Tue Jun 09, 13:11"}
    ];

    const mockFetch = 'mockFetch'

    const gitBranches = ["master", "dev"];

    const reports = [
        {name: "report1", date: new Date().toISOString()},
        {name: "report2", date: null}
    ];

    const getWrapper = (reports) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": reports});

        return mount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    instances_supported: false
                },
                gitBranches
            }
        });
    }

    it("renders git branch drop down and fetches commits if git supported", (done) => {

        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);
        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master");
        expect(options.at(0).attributes().value).toBe("master");
        expect(options.at(1).text()).toBe("dev");
        expect(options.at(1).attributes().value).toBe("dev");

        setTimeout(() => {
            expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
            const commitOptions = wrapper.findAll("#git-commit option");
            expect(commitOptions.length).toBe(2);
            expect(commitOptions.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
            expect(commitOptions.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

            expect(wrapper.vm.selectedCommitId).toBe("abcdef");
            done();
        })
    });

    it("does not render git drop downs if git not supported", async () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: false, instances_supported: false},
                gitBranches: null
            }
        });

        await Vue.nextTick();
        expect(mockAxios.history.get.length).toBe(1);
        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });

    it("calls api to get commits when branch changes and updates commits drop down", (done) => {
        mockAxios.onGet('http://app/git/branch/dev/commits/')
            .reply(200, {"data": gitCommits});
        mockAxios.onGet('http://app/reports/runnable/?branch=dev&commit=abcdef')
            .reply(200, {"data": []});

        const wrapper = mount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        wrapper.findAll("#git-branch option").at(1).setSelected();

        expect(wrapper.vm.selectedBranch).toBe("dev");

        setTimeout(() => {
            const options = wrapper.findAll("#git-commit option");
            expect(options.length).toBe(2);
            expect(options.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
            expect(options.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

            expect(wrapper.vm.selectedCommitId).toBe("abcdef");

            expect(wrapper.find(ErrorInfo).props("apiError")).toBe("");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("");
            done();
        })
    });

    it("show error message if error getting git commits", (done) => {
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching Git commits");
            done();
        })
    });

    it("renders refresh git button if git_supported and posts git fetch to api on click ", (done) => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {git_supported: true, instances_supported: false},
                gitBranches
            }
        });

        expect(wrapper.find("#git-refresh-btn").exists()).toBe(true);
        const button = wrapper.find("#git-refresh-btn");
        expect(mockAxios.history.post.length).toBe(0);
        expect(mockAxios.history.get.length).toBe(0);
        
        button.trigger("click")
        
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(4);
            done();
        })
    });
    
    it("updates reports dropdown by calling api when commit changes", (done) => {
        const wrapper = getWrapper(reports);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(ErrorInfo).props("apiError")).toBe("");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("");
            expect(wrapper.find(ReportList).props("reports")).toEqual(expect.arrayContaining(reports));
            done();
        });
    });

    it("displays report list in order and allows selection and reset", (done) => {
        const wrapper = getWrapper(reports);

        setTimeout(async () => {
            wrapper.find(ReportList).find("a:last-of-type").trigger("click");
            expect(wrapper.vm.$data["selectedReport"]).toBe("report2");
            await Vue.nextTick();
            wrapper.find(ReportList).find("button").trigger("click");
            expect(wrapper.vm.$data["selectedReport"]).toBe("");
            done();
        });
    });

    it("shows instances if instances supported", () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: false,
                    instances_supported: true,
                    instances: {
                        source: ["prod", "uat"],
                        annex: ["one"],
                        another: []
                    }
                },
                gitBranches
            },
            data() {
                return {
                    selectedReport: "report1"
                }
            }
        });

        const sourceOptions = wrapper.findAll("#source option");
        expect(sourceOptions.length).toBe(2);
        expect(sourceOptions.at(0).attributes().value).toBe("prod");
        expect(sourceOptions.at(0).text()).toBe("prod");
        expect(sourceOptions.at(1).attributes().value).toBe("uat");
        expect(sourceOptions.at(1).text()).toBe("uat");

        expect(wrapper.find("#annex").exists()).toBe(false); // only 1 option so don't show
        expect(wrapper.find("#another").exists()).toBe(false); // no options so don't show
    });

    it("doesn't show instances if instances not supported", () => {
        const wrapper = shallowMount(RunReport, {
            propsData: {
                metadata: {
                    git_supported: true,
                    instances_supported: false,
                    instances: {
                        source: ["prod", "uat"],
                        annex: ["one"],
                        another: []
                    }
                },
                gitBranches
            }
        });

        expect(wrapper.find("#source").exists()).toBe(false);
        expect(wrapper.find("#annex").exists()).toBe(false);
        expect(wrapper.find("#another").exists()).toBe(false);
    });

});
