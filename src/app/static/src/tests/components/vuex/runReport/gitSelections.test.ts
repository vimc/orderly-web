import { shallowMount } from "@vue/test-utils";
import GitSelections from "../../../../js/components/vuex/runReport/gitSelections.vue";
import Vuex from "vuex";
import {mockGitState, RecursivePartial} from "../../../mocks";
import {GitState} from "../../../../js/store/git/git";
import {namespace} from "../../../../js/store/runReport/store";

describe("GitSelections", () => {

    const mockActions = {
        FetchMetadata: jest.fn(),
        SelectBranch: jest.fn(),
        RefreshGit: jest.fn(),
    };

    const createStore = (gitState: RecursivePartial<GitState>, actions = mockActions, mutations = {}) => {
        return new Vuex.Store({
            modules: {
                [namespace.git]: {
                    namespaced: true,
                    state: mockGitState(gitState),
                    mutations,
                    actions
                }
            }
        })
    }

    const commits = [
        {id: "commit1", date_time: "time1", age: 1},
        {id: "commit2", date_time: "time2", age: 2}
    ]

    it("renders git branch drop down if git supported", () => {

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: true
                },
                branches: ["master", "dev"]
            })
        })

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(true);
        const options = wrapper.findAll("#git-branch-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("master");
        expect(options.at(0).attributes().value).toBe("master");
        expect(options.at(1).text()).toBe("dev");
        expect(options.at(1).attributes().value).toBe("dev");
    });

    it("does not render git branch drop down if git not supported", () => {

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: false
                },
                branches: ["master", "dev"]
            })
        })

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
    });

    it("renders git commit drop down if has commits", () => {

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                commits
            })
        })

        expect(wrapper.find("#git-commit-form-group").exists()).toBe(true);
        const options = wrapper.findAll("#git-commit-form-group select option");
        expect(options.length).toBe(2);
        expect(options.at(0).text()).toBe("commit1 (time1)");
        expect(options.at(0).attributes().value).toBe("commit1");
        expect(options.at(1).text()).toBe("commit2 (time2)");
        expect(options.at(1).attributes().value).toBe("commit2");
    });

    it("does not render git commit drop down if has no commits", () => {

        const wrapper = shallowMount(GitSelections, {
            store: createStore({})
        })

        expect(wrapper.find("#git-commit-form-group").exists()).toBe(false);
    });



    it("dropdown selection reflects git branch and commit selection in store", () => {

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                branches: ["master", "dev"],
                commits,
                selectedBranch: "dev",
                selectedCommit: "commit2"
            })
        })

        const branchOptions = wrapper.findAll("#git-branch-form-group select option");
        expect((branchOptions.at(0).element as HTMLOptionElement).selected).toBe(false);
        expect(branchOptions.at(1).attributes().value).toBe("dev");
        expect((branchOptions.at(1).element as HTMLOptionElement).selected).toBe(true);

        const commitOptions = wrapper.findAll("#git-commit-form-group select option");
        expect((commitOptions.at(0).element as HTMLOptionElement).selected).toBe(false);
        expect(commitOptions.at(1).attributes().value).toBe("commit2");
        expect((commitOptions.at(1).element as HTMLOptionElement).selected).toBe(true);
    });

    it("selecting a git branch triggers select branch action", () => {
        const selectBranchMock = jest.fn();

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: true
                },
                branches: ["master", "dev"]
            },
                {
                    ...mockActions,
                    SelectBranch: selectBranchMock
                })
        })
        wrapper.findAll("#git-branch option").at(1).setSelected();
        expect(selectBranchMock.mock.calls.length).toBe(1);
        expect(selectBranchMock.mock.calls[0][1]).toBe("dev");
    });

    it("selecting a git commit triggers select commit mutation", () => {
        const selectCommitMock = jest.fn();
        const store = createStore({
            metadata: {
                git_supported: true,
            },
            branches: ["master", "dev"],
            commits
        },
            mockActions,
            {
                SelectCommit: selectCommitMock
            })

        const wrapper = shallowMount(GitSelections, {
            store
        })
        wrapper.findAll("#git-commit option").at(1).setSelected();
        expect(selectCommitMock.mock.calls.length).toBe(1);
        expect(selectCommitMock.mock.calls[0][1]).toBe("commit2");
    });

    it("clicking refresh git button triggers refresh git action", async () => {
        const refreshGitMock = jest.fn();

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: true
                },
                branches: ["master", "dev"]
            },
                {
                    ...mockActions,
                    RefreshGit: refreshGitMock
                })
        })
        const refreshGitBtn = wrapper.find("#git-refresh-btn")
        expect(refreshGitBtn.text()).toBe("Refresh git");
        await refreshGitBtn.trigger("click");
        expect(refreshGitMock.mock.calls.length).toBe(1);
    });

    it("refresh git button is disabled if gitRefreshing in state", () => {
        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: true
                },
                branches: ["master", "dev"],
                gitRefreshing: true
            })
        })
        const refreshGitBtn = wrapper.find("#git-refresh-btn")
        expect(refreshGitBtn.text()).toBe("Fetching...");
        expect(refreshGitBtn.attributes("disabled")).toBe("disabled");
    });
})
