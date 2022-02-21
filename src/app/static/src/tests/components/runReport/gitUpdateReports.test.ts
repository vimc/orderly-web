import {mockAxios} from "../../mockAxios";
import {mount, shallowMount} from "@vue/test-utils";
import GitUpdateReports from "../../../js/components/runReport/gitUpdateReports.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import Vue from "vue";
import {ReportsState} from "../../../js/utils/types";
import Vuex from "vuex";
import {mockReportsState} from "../../mocks";
import {ReportsAction} from "../../../js/store/reports/actions";

describe("gitUpdateReports", () => {
    const reports = [
        {name: "report1", date: new Date()},
        {name: "report2", date: null}
    ];

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(200, {"data": gitCommits});
        jest.resetAllMocks();
    });

    const gitCommits = [
        {id: "abcdef", date_time: "Mon Jun 08, 12:01"},
        {id: "abc123", date_time: "Tue Jun 09, 13:11"}
    ];

    const initialBranches = ["master", "dev"];
    const initialBranch = "master";
    const initialCommitId = "abc123";
    const showAllReports = false;

    const props = {
        reportMetadata: {
            git_supported: true,
            instances_supported: false
        },
        initialBranches,
        initialBranch,
        initialCommitId,
        showAllReports
    };

    const mockFetchReports = jest.fn();

    const createStore = (state: Partial<ReportsState> = {}) => {
        return new Vuex.Store({
            state: {},
            modules: {
                reports: {
                    namespaced: true,
                    state: mockReportsState(state),
                    actions: {
                        [ReportsAction.FetchRunnableReports]: mockFetchReports
                    }
                }
            }
        });
    };

    const getWrapper = (report = reports, propsData = props) => {
        return mount(GitUpdateReports, {
            store: createStore({runnableReports: report}),
            propsData
        });
    };

    it("renders git branch drop down and fetches commits if git supported", async () => {

        const wrapper = getWrapper([], props);

        await Vue.nextTick();

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);
        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master");
        expect(options.at(0).attributes().value).toBe("master");
        expect(options.at(1).text()).toBe("dev");
        expect(options.at(1).attributes().value).toBe("dev");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
        const commitOptions = wrapper.findAll("#git-commit option");
        expect(commitOptions.length).toBe(2);
        expect(commitOptions.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
        expect(commitOptions.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

        expect(wrapper.vm.$data.selectedCommitId).toBe("abc123");
    });

    it("does not render git drop downs if git not supported", async () => {
        const wrapper = shallowMount(GitUpdateReports, {
            propsData: {
                reportMetadata: {git_supported: false, instances_supported: false},
                initialBranches: null
            },
            store: createStore()
        });

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });

    it("emits expected events on mount when initial values provided", async () => {
        const wrapper = getWrapper();

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.emitted("branchSelected")?.length).toBe(1);
        expect(wrapper.emitted("branchSelected")![0][0]).toBe(initialBranch);

        expect(wrapper.emitted("commitSelected")?.length).toBe(1);
        expect(wrapper.emitted("commitSelected")![0][0]).toBe(initialCommitId);

        expect(mockFetchReports.mock.calls.length).toBe(1);
        expect(mockFetchReports.mock.calls[0][1]).toEqual({
            branch: "master",
            commit: "abc123",
            showAll: false
        });
    });

    it("emits expected events on mount when no initial values provided", async () => {
        const wrapper = getWrapper(reports, {
            ...props,
            initialBranch: "",
            initialCommitId: ""
        });

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.emitted("branchSelected")?.length).toBe(1);
        expect(wrapper.emitted("branchSelected")![0][0]).toBe("master");

        expect(wrapper.emitted("commitSelected")?.length).toBe(1);
        expect(wrapper.emitted("commitSelected")![0][0]).toBe("abcdef");

        expect(mockFetchReports.mock.calls.length).toBe(1);
        expect(mockFetchReports.mock.calls[0][1]).toEqual({
            branch: "master",
            commit: "abcdef",
            showAll: false
        });
    });

    it("updates reports when git not supported", async () => {
        const wrapper = shallowMount(GitUpdateReports, {
            propsData: {
                ...props,
                reportMetadata: {git_supported: false, instances_supported: false},
                initialBranches: null
            },
            store: createStore()
        });

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.emitted("branchSelected")).toBe(undefined);
        expect(wrapper.emitted("commitSelected")).toBe(undefined);

        expect(mockFetchReports.mock.calls.length).toBe(1);
        expect(mockFetchReports.mock.calls[0][1]).toEqual({
            branch: "",
            commit: "",
            showAll: false
        });
    });

    it("defaults to first commit if initial commit not found", async () => {

        getWrapper([], {...props, initialCommitId: "non-existent"});

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockFetchReports.mock.calls.length).toBe(1);
        expect(mockFetchReports.mock.calls[0][1]).toEqual({
            branch: "master",
            commit: "abcdef",
            showAll: false
        });
    });

    it("calls api to get commits when branch changes and updates commits drop down", async () => {
        const devCommits = [
            {id: "bcdefg", date_time: "Mon Jun 08, 12:01"},
            {id: "bcd123", date_time: "Tue Jun 09, 13:11"}
        ];
        mockAxios.onGet('http://app/git/branch/dev/commits/')
            .reply(200, {"data": devCommits});

        const wrapper = getWrapper();

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        let options = wrapper.findAll("#git-commit option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("abcdef (Mon Jun 08, 12:01)");
        expect(options.at(1).text()).toBe("abc123 (Tue Jun 09, 13:11)");

        await wrapper.findAll("#git-branch option").at(1).setSelected();

        await Vue.nextTick();
        await Vue.nextTick();

        options = wrapper.findAll("#git-commit option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("bcdefg (Mon Jun 08, 12:01)");
        expect(options.at(1).text()).toBe("bcd123 (Tue Jun 09, 13:11)");
        expect(wrapper.findComponent(ErrorInfo).props("apiError")).toBe("");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("");

        expect(wrapper.emitted("branchSelected")![1][0]).toBe("dev");
        expect(wrapper.emitted("commitSelected")![1][0]).toBe("bcdefg");

    });

    it("updates reports when commit changes", async () => {

        const wrapper = getWrapper();

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockFetchReports.mock.calls.length).toBe(1);
        expect(mockFetchReports.mock.calls[0][1]).toEqual({
            branch: "master",
            commit: "abc123",
            showAll: false
        });

        wrapper.findAll("#git-commit option").at(0).setSelected();

        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockFetchReports.mock.calls.length).toBe(2);
        expect(mockFetchReports.mock.calls[1][1]).toEqual({
            branch: "master",
            commit: "abcdef",
            showAll: false
        });

    });

    it("shows error message if error getting git commits", async () => {
        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(GitUpdateReports, {
            propsData: {
                reportMetadata: {git_supported: true, instances_supported: false},
                initialBranches
            },
            store: createStore()
        });

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred fetching Git commits");
    });

    it("renders refresh git button if git_supported and fetches on click ", async () => {
        mockAxios.onGet('http://app/git/fetch/')
            .reply(200, {"data": [{name: "master2"}, {name: "dev2"}]});

        const wrapper = getWrapper();

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        let options = wrapper.findAll("#git-branch-form-group select option");

        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master");
        expect(options.at(0).attributes().value).toBe("master");
        expect(options.at(1).text()).toBe("dev");
        expect(options.at(1).attributes().value).toBe("dev");

        const button = wrapper.find("#git-refresh-btn");

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);

        expect(button.attributes("disabled")).toBeUndefined();
        expect(button.text()).toBe("Refresh git");

        await button.trigger("click");

        expect(button.attributes("disabled")).toBe("disabled");
        expect(button.text()).toBe("Fetching...");

        await Vue.nextTick();
        await Vue.nextTick();

        expect(button.attributes("disabled")).toBeUndefined();
        expect(button.text()).toBe("Refresh git");

        options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master2");
        expect(options.at(0).attributes().value).toBe("master2");
        expect(options.at(1).text()).toBe("dev2");
        expect(options.at(1).attributes().value).toBe("dev2");
    });

    it("show error message if error refreshing git", async () => {
        mockAxios.onGet('http://app/git/fetch/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(GitUpdateReports, {
            propsData: {
                reportMetadata: {git_supported: true, instances_supported: false},
                initialBranches
            },
            store: createStore()
        });

        const button = wrapper.find("#git-refresh-btn");

        await button.trigger("click");

        await Vue.nextTick();

        expect(button.attributes("disabled")).toBe("disabled");

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(button.attributes("disabled")).toBeUndefined();
        expect(wrapper.findComponent(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
        expect(wrapper.findComponent(ErrorInfo).props("defaultMessage")).toBe("An error occurred refreshing Git");
    });

    it("gets reports with show_all flag when showAllReports prop is true", async () => {

        shallowMount(GitUpdateReports, {
            propsData: {
                reportMetadata: {git_supported: true, instances_supported: false},
                initialBranches,
                initialBranch,
                initialCommitId,
                showAllReports: true
            },
            store: createStore()
        });

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();

        expect(mockFetchReports.mock.calls.length).toBe(1);
        expect(mockFetchReports.mock.calls[0][1]).toEqual({
            branch: "master",
            commit: "abc123",
            showAll: true
        });
    });

});
