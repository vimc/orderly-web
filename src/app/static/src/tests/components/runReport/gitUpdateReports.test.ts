import {mockAxios} from "../../mockAxios";
import {mount, shallowMount} from "@vue/test-utils";
import GitUpdateReports from "../../../js/components/runReport/gitUpdateReports.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import Vue from "vue";
import Vuex from "vuex";
import {mockGitState, mockRunReportMetadata, mockRunReportRootState} from "../../mocks";
import {GitState} from "../../../js/store/git/git";

describe("gitUpdateReports", () => {
    const reports = [
        {name: "report1", date: new Date().toISOString()},
        {name: "report2", date: null}
    ];

    const initialBranches = ["master", "dev"];
    const gitState = {
        git_branches: initialBranches,
        metadata: {
            git_supported: true,
            instances_supported: false,
            instances: {"source": []},
            changelog_types: ["published", "internal"]
        }
    }

    const createStore = (state: Partial<GitState> = gitState) => {
        return new Vuex.Store({
            state: {},
            modules: {
                git: {
                    namespaced: true,
                    state: mockGitState(state)
                }
            }
        });
    };

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(200, {"data": gitCommits});
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abc123')
            .reply(200, {"data": reports});
    });

    const gitCommits = [
        {id: "abcdef", date_time: "Mon Jun 08, 12:01"},
        {id: "abc123", date_time: "Tue Jun 09, 13:11"}
    ];

    const initialBranch = "master";
    const initialCommitId = "abc123";
    const showAllReports = false;

    const props = {
        initialBranch,
        initialCommitId,
        showAllReports
    };

    const getWrapper = (report = reports, propsData = props, state = gitState) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abc123')
            .reply(200, {"data": report});

        return mount(GitUpdateReports, {
            propsData,
            store: createStore(state)
        });
    };

    it("renders git branch drop down and fetches commits if git supported", async (done) => {

        const wrapper = getWrapper([], props);

        await Vue.nextTick();

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

            expect(wrapper.vm.$data.selectedCommitId).toBe("abc123");
            done();
        })
    });

    it("does not render git drop downs if git not supported", async () => {
        const wrapper = shallowMount(GitUpdateReports, {
            store: createStore({
                metadata: {git_supported: false, instances_supported: false, changelog_types: [], instances: {}},
                git_branches: null
            })
        });

        await Vue.nextTick();
        expect(mockAxios.history.get.length).toBe(1);
        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });

    it("emits expected events on mount when initial values provided", (done) => {
        const wrapper = getWrapper();
        expect(wrapper.emitted("branchSelected")?.length).toBe(1);
        expect(wrapper.emitted("branchSelected")![0][0]).toBe(initialBranch);

        setTimeout(() => {
            expect(wrapper.emitted("commitSelected")?.length).toBe(1);
            expect(wrapper.emitted("commitSelected")![0][0]).toBe(initialCommitId);

            expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
            expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual(reports);
            done();
        });
    });

    it("emits expected events on mount when no initial values provided", (done) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": reports});
        const wrapper = getWrapper(reports, {
            ...props,
            initialBranch: "",
            initialCommitId: ""
        });
        expect(wrapper.emitted("branchSelected")?.length).toBe(1);
        expect(wrapper.emitted("branchSelected")![0][0]).toBe("master");
        setTimeout(() => {
            expect(wrapper.emitted("commitSelected")?.length).toBe(1);
            expect(wrapper.emitted("commitSelected")![0][0]).toBe("abcdef");

            expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
            expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual(reports);
            done();
        });
    });

    it("updates reports when git not supported", (done) => {
        mockAxios.onGet('http://app/reports/runnable/')
            .reply(200, {"data": [reports[0]]});
        const wrapper = shallowMount(GitUpdateReports,
            {
                store: createStore({
                    metadata: {
                        git_supported: false,
                        instances_supported: false,
                        instances: {},
                        changelog_types: []
                    },
                    git_branches: null
                })
            });
        setTimeout(() => {
            expect(wrapper.emitted("branchSelected")).toBe(undefined);
            expect(wrapper.emitted("commitSelected")).toBe(undefined);
            expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
            expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual([reports[0]]);
            done();
        });
    });

    it("defaults to first commit if initial commit not found", (done) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": reports});

        const wrapper = getWrapper([], {...props, initialCommitId: "non-existent"});
        setTimeout(() => {
            expect(wrapper.vm.$data["selectedCommitId"]).toBe("abcdef");
            expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
            expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual(reports);
            done();
        });
    });

    it("calls api to get commits when branch changes and updates commits drop down", async (done) => {
        const devCommits = [
            {id: "bcdefg", date_time: "Mon Jun 08, 12:01"},
            {id: "bcd123", date_time: "Tue Jun 09, 13:11"}
        ];
        mockAxios.onGet('http://app/git/branch/dev/commits/')
            .reply(200, {"data": devCommits});
        mockAxios.onGet('http://app/reports/runnable/?branch=dev&commit=bcdefg')
            .reply(200, {"data": reports});

        const wrapper = getWrapper();

        setTimeout(() => {
            wrapper.findAll("#git-branch option").at(1).setSelected();

            expect(wrapper.vm.$data.selectedBranch).toBe("dev");

            setTimeout(() => {
                const options = wrapper.findAll("#git-commit option");
                expect(options.length).toBe(2);
                expect(options.at(0).text()).toBe("bcdefg (Mon Jun 08, 12:01)");
                expect(options.at(1).text()).toBe("bcd123 (Tue Jun 09, 13:11)");

                expect(wrapper.vm.$data.selectedCommitId).toBe("bcdefg");

                expect(wrapper.findComponent(ErrorInfo).props("apiError")).toBe("");
                expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("");

                expect(wrapper.emitted("branchSelected")![1][0]).toBe("dev");
                expect(wrapper.emitted("commitSelected")![1][0]).toBe("bcdefg");

                done();
            });
        });
    });

    it("updates reports when commit changes", (done) => {
        const newReports = [
            {name: "report3", date: null},
            {name: "report4", date: null}
        ];
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": newReports});
        const wrapper = getWrapper();

        setTimeout(() => {
            wrapper.findAll("#git-commit option").at(0).setSelected();
            expect(wrapper.vm.$data.selectedCommitId).toBe("abcdef");
            setTimeout(() => {
                expect(wrapper.vm.$data.reports).toStrictEqual(newReports);
                expect(wrapper.emitted("reportsUpdate")![1][0]).toStrictEqual(newReports);
                done();
            });
        });
    });

    it("shows error message if error getting git commits", (done) => {
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(GitUpdateReports, {
            store: createStore({
                metadata: {git_supported: true, instances_supported: false, changelog_types: [], instances: {}},
                git_branches: initialBranches
            })
        });

        setTimeout(() => {
            expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching Git commits");
            done();
        })
    });

    it("shows error message if error fetching runnable reports", (done) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(GitUpdateReports, {
            store: createStore({
                metadata: {git_supported: true, instances_supported: false, changelog_types: [], instances: {}},
                git_branches: initialBranches
            })
        });

        setTimeout(() => {
            expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching reports");
            done();
        });
    });


    it("renders refresh git button if git_supported and fetches on click ", async (done) => {
        const mockFetch = [{name: "master2"}, {name: "dev2"}]
        mockAxios.onGet('http://app/git/fetch/')
            .reply(200, {"data": mockFetch});

        const wrapper = getWrapper();

        setTimeout(() => {

            expect(wrapper.find("#git-refresh-btn").exists()).toBe(true);
            const button = wrapper.find("#git-refresh-btn");
            expect(mockAxios.history.get.length).toBe(2);

            expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);

            expect(wrapper.vm.$data.gitRefreshing).toBe(false);
            expect(button.attributes("disabled")).toBeUndefined();
            expect(button.text()).toBe("Refresh git");


            setTimeout(async () => { //give the wrapper time to fetch reports
                button.trigger("click")
                expect(wrapper.vm.$data.gitRefreshing).toBe(true);
                await Vue.nextTick();
                expect(button.attributes("disabled")).toBe("disabled");
                expect(button.text()).toBe("Fetching...");

                setTimeout(() => {
                    const getHistory = mockAxios.history.get
                    expect(getHistory[getHistory.length - 2].url).toBe("http://app/git/fetch/");
                    expect(getHistory[getHistory.length - 1].url).toBe("http://app/git/branch/master2/commits/");
                    expect(wrapper.vm.$data.gitRefreshing).toBe(false);
                    expect(button.attributes("disabled")).toBeUndefined();
                    expect(button.text()).toBe("Refresh git");

                    const options = wrapper.findAll("#git-branch-form-group select option");
                    expect(options.length).toBe(2);
                    expect(options.at(0).text()).toBe("master2");
                    expect(options.at(0).attributes().value).toBe("master2");
                    expect(options.at(1).text()).toBe("dev2");
                    expect(options.at(1).attributes().value).toBe("dev2");
                    done();
                });
            });
        });
    });

    it("show error message if error refreshing git", async (done) => {
        mockAxios.onGet('http://app/git/fetch/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(GitUpdateReports, {
            store: createStore({
                metadata: {git_supported: true, instances_supported: false, changelog_types: [], instances: {}},
                git_branches: initialBranches
            })
        });
        expect(wrapper.find("#git-refresh-btn").exists()).toBe(true);
        const button = wrapper.find("#git-refresh-btn");
        setTimeout(async () => { //give the wrapper time to fetch reports
            button.trigger("click")
            expect(wrapper.vm.$data.gitRefreshing).toBe(true);
            await Vue.nextTick();
            expect(button.attributes("disabled")).toBe("disabled");

            setTimeout(() => {
                const getHistory = mockAxios.history.get
                expect(getHistory[getHistory.length - 2].url).toBe("http://app/reports/runnable/?branch=master&commit=abcdef");
                expect(getHistory[getHistory.length - 1].url).toBe("http://app/git/fetch/");
                expect(wrapper.vm.$data.gitRefreshing).toBe(false);
                expect(button.attributes("disabled")).toBeUndefined();
                expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
                expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred refreshing Git");
                expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
                expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred refreshing Git");
                done();
            })
        })
    });

    it("gets reports with show_all flag when showAllReports prop is true", (done) => {
        const url = 'http://app/reports/runnable/?branch=master&commit=abc123&show_all=true';
        mockAxios.onGet(url)
            .reply(200, {"data": [reports[0]]});
        const wrapper = shallowMount(GitUpdateReports, {
            propsData: {
                initialBranch,
                initialCommitId,
                showAllReports: true
            },
            store: createStore({
                metadata: {git_supported: true, instances_supported: false, changelog_types: [], instances: {}},
                git_branches: initialBranches,
            })
        });
        setTimeout(() => {
            const getHistory = mockAxios.history.get;
            expect(getHistory[getHistory.length - 1].url).toBe(url);
            expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual([reports[0]]);
            done();
        });
    });

    it("gets reports without show_all flag when showAllReports prop is true but git_supported is false", (done) => {
        const url = 'http://app/reports/runnable/';
        mockAxios.onGet(url)
            .reply(200, {"data": [reports[0]]});
        const wrapper = shallowMount(GitUpdateReports, {
            propsData: {
                showAllReports: true
            },
            store: createStore({
                metadata: {git_supported: false, instances_supported: false, instances: {}, changelog_types: []},
                git_branches: null,
            })
        });
        setTimeout(() => {
            const getHistory = mockAxios.history.get;
            expect(getHistory[getHistory.length - 1].url).toBe(url);
            expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual([reports[0]]);
            done();
        });
    });
});
