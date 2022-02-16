import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import GitUpdateReports from "../../../js/components/runReport/gitUpdateReports.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import Vue from "vue";
import {GitState} from "../../../js/store/git/git";
import Vuex from "vuex";
import {mockGitState} from "../../mocks";
import {GitMutation} from "../../../js/store/git/mutations";

describe("gitUpdateReports", () => {
    const reports = [
        {name: "report1", date: new Date().toISOString()},
        {name: "report2", date: null}
    ];

    const gitState = mockGitState({
        gitBranches: ["master", "dev"],
        selectedBranch: "master",
        selectedCommitId: "abc123"
    })

    const mockSelectBranch = jest.fn();
    const mockSelectCommitId = jest.fn();

    const createStore = (state: Partial<GitState> = gitState) => {
        return new Vuex.Store({
            state: {},
            modules: {
                git: {
                    namespaced: true,
                    state: mockGitState(state),
                    mutations: {
                        [GitMutation.SelectBranch]: mockSelectBranch,
                        [GitMutation.SelectCommitId]: mockSelectCommitId
                    }
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
        jest.resetAllMocks();
    });

    const gitCommits = [
        {id: "abcdef", date_time: "Mon Jun 08, 12:01"},
        {id: "abc123", date_time: "Tue Jun 09, 13:11"}
    ];

    const showAllReports = false;

    const props = {
        showAllReports
    };

    const getWrapper = async (propsData = props, state: Partial<GitState> = gitState) => {

        const wrapper = shallowMount(GitUpdateReports, {
            store: createStore(state),
            propsData
        });

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        return wrapper;
    };

    it("renders git branch drop down and fetches commits if git supported", async () => {

        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": reports});

        const wrapper = await getWrapper(props, {
            metadata: {
                git_supported: true,
                instances_supported: false,
                changelog_types: [],
                instances: {}
            },
            selectedCommitId: "abcdef"
        });

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);
        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master");
        expect(options.at(0).attributes().value).toBe("master");
        expect(options.at(1).text()).toBe("dev");
        expect(options.at(1).attributes().value).toBe("dev");

        expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
        const commitOptions = wrapper.findAll("#git-commit option");
        expect(commitOptions.length).toBe(2);
        expect(commitOptions.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
        expect(commitOptions.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

        // also expect reports updated with the results from the  above GET request
        expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
        expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual(reports);
    });

    it("defaults to first commit if selected commit not found", async () => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": reports});

        const wrapper = await getWrapper(props, {selectedCommitId: "non-existent"});

        expect(mockSelectCommitId.mock.calls[0][1]).toBe("abcdef");

        // also expect reports updated with the results from the  above GET request
        expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
        expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual(reports);
    });

    it("does not render git drop downs if git not supported", async () => {
        const wrapper = shallowMount(GitUpdateReports, {
            store: createStore({
                metadata: {
                    git_supported: false,
                    instances_supported: false,
                    changelog_types: [],
                    instances: {}
                }
            })
        });

        await Vue.nextTick();
        expect(mockAxios.history.get.length).toBe(1);
        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });

    it("updates reports when git not supported", async () => {
        mockAxios.onGet('http://app/reports/runnable/')
            .reply(200, {"data": [reports[0]]});
        const wrapper = await getWrapper(props, {
            metadata: {
                git_supported: false,
                instances_supported: false,
                changelog_types: [],
                instances: {}
            }
        });

        expect(wrapper.emitted("reportsUpdate")?.length).toBe(1);
        expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual([reports[0]]);
    });

    it("calls api to get commits when branch changes and updates commits drop down", async () => {
        const devCommits = [
            {id: "bcdefg", date_time: "Mon Jun 08, 12:01"},
            {id: "bcd123", date_time: "Tue Jun 09, 13:11"}
        ];
        mockAxios.onGet('http://app/git/branch/dev/commits/')
            .reply(200, {"data": devCommits});
        mockAxios.onGet('http://app/reports/runnable/?branch=dev&commit=bcdefg')
            .reply(200, {"data": reports});

        const wrapper = await getWrapper();

        await wrapper.findAll("#git-branch option").at(1).setSelected();
        expect(mockSelectBranch.mock.calls[0][1]).toBe("dev");

        await Vue.nextTick();
        await Vue.nextTick();

        const options = wrapper.findAll("#git-commit option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("bcdefg (Mon Jun 08, 12:01)");
        expect(options.at(1).text()).toBe("bcd123 (Tue Jun 09, 13:11)");

        expect(mockSelectCommitId.mock.calls[0][1]).toBe("bcdefg");

        expect(wrapper.findComponent(ErrorInfo).props("apiError")).toBe("");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("");

    });

    it("updates reports when commit changes", async (done) => {
        const newReports = [
            {name: "report3", date: null},
            {name: "report4", date: null}
        ];
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef')
            .reply(200, {"data": newReports});

        const wrapper = await getWrapper();

        expect(wrapper.vm.$data.reports).toStrictEqual(reports);
        expect(mockAxios.history.get.length).toBe(2);
        expect(mockAxios.history.get[1].url).toBe("http://app/reports/runnable/?branch=master&commit=abc123");
        expect(mockSelectCommitId.mock.calls.length).toBe(0);

        await wrapper.findAll("#git-commit option").at(0).setSelected();
        expect(mockSelectCommitId.mock.calls[0][1]).toBe("abcdef");

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(3);
            expect(mockAxios.history.get[2].url).toBe("http://app/reports/runnable/?branch=master&commit=abcdef");
            expect(wrapper.vm.$data.reports).toStrictEqual(newReports);
            expect(wrapper.emitted("reportsUpdate")![1][0]).toStrictEqual(newReports);
            done();
        });

    });

    it("shows error message if error getting git commits", async () => {
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(500, "TEST ERROR");

        const wrapper = await getWrapper();

        expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching Git commits");
    });

    it("shows error message if error fetching runnable reports", async () => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abc123')
            .reply(500, "TEST ERROR");

        const wrapper = await getWrapper();

        await Vue.nextTick();

        expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching reports");
    });

    it("renders refresh git button if git_supported and fetches on click ", async () => {
        const mockFetch = [{name: "master2"}, {name: "dev2"}]
        mockAxios.onGet('http://app/git/fetch/')
            .reply(200, {"data": mockFetch});

        const wrapper = await getWrapper();

        expect(mockAxios.history.get.length).toBe(2);
        expect(mockAxios.history.get[0].url).toBe("http://app/git/branch/master/commits/");
        expect(mockAxios.history.get[1].url).toBe("http://app/reports/runnable/?branch=master&commit=abc123");

        const button = wrapper.find("#git-refresh-btn");
        expect(button.exists()).toBe(true);
        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);

        expect(button.attributes("disabled")).toBeUndefined();
        expect(button.text()).toBe("Refresh git");

        await button.trigger("click");

        expect(button.attributes("disabled")).toBe("disabled");
        expect(button.text()).toBe("Fetching...");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.get.length).toBe(4);
        expect(mockAxios.history.get[2].url).toBe("http://app/git/fetch/");
        expect(mockAxios.history.get[3].url).toBe("http://app/git/branch/master2/commits/");

        expect(button.attributes("disabled")).toBeUndefined();
        expect(button.text()).toBe("Refresh git");

        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master2");
        expect(options.at(0).attributes().value).toBe("master2");
        expect(options.at(1).text()).toBe("dev2");
        expect(options.at(1).attributes().value).toBe("dev2");
    });

    it("show error message if error refreshing git", async () => {
        mockAxios.onGet('http://app/git/fetch/')
            .reply(500, "TEST ERROR");

        const wrapper = await getWrapper();
        const button = wrapper.find("#git-refresh-btn");

        await button.trigger("click")
        expect(button.attributes("disabled")).toBe("disabled");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockAxios.history.get[2].url).toBe("http://app/git/fetch/");
        expect(mockAxios.history.get.length).toBe(3);

        await Vue.nextTick();
        await Vue.nextTick();

        expect(button.attributes("disabled")).toBeUndefined();

        expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred refreshing Git");
    });

    it("gets reports with show_all flag when showAllReports prop is true", async () => {
        const url = 'http://app/reports/runnable/?branch=master&commit=abc123&show_all=true';
        mockAxios.onGet(url)
            .reply(200, {"data": [reports[0]]});
        const wrapper = await getWrapper({showAllReports: true});
        const getHistory = mockAxios.history.get;
        expect(getHistory[getHistory.length - 1].url).toBe(url);
        expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual([reports[0]]);
    });

    it("gets reports without show_all flag when showAllReports prop is true but git_supported is false", async () => {
        const url = 'http://app/reports/runnable/';
        mockAxios.onGet(url)
            .reply(200, {"data": [reports[0]]});
        const wrapper = await getWrapper({showAllReports: true},
            {
                metadata: {
                    git_supported: false,
                    instances_supported: false,
                    changelog_types: [],
                    instances: {}
                }
            });
        const getHistory = mockAxios.history.get;
        expect(getHistory[getHistory.length - 1].url).toBe(url);
        expect(wrapper.emitted("reportsUpdate")![0][0]).toStrictEqual([reports[0]]);
    });
});
