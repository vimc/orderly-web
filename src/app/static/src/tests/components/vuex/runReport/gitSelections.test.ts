import Vue from "vue";
import { shallowMount } from "@vue/test-utils";
import GitSelections from "../../../../js/components/vuex/runReport/gitSelections.vue";
import Vuex from "vuex";
import {mockGitState, RecursivePartial} from "../../../mocks";
import {GitState} from "../../../../js/store/git/git";
import {mutations} from "../../../../js/store/git/mutations";
import {namespace} from "../../../../js/store/runReport/store";

describe("GitSelections", () => {

    const mockActions = {
        FetchMetadata: jest.fn(),
        SelectBranch: jest.fn(),
        SelectCommit: jest.fn()
    };

    const createStore = (gitState: RecursivePartial<GitState>, actions = mockActions) => {
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

    it("first git branch is selected automatically and selecting a git branch triggers select branch action", async () => {
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
        await Vue.nextTick();
        expect(wrapper.vm.$data.newSelectedBranch).toBe("master");
        expect(selectBranchMock.mock.calls.length).toBe(1);
        expect(selectBranchMock.mock.calls[0][1]).toBe("master");
        wrapper.findAll("#git-branch option").at(1).setSelected();
        await Vue.nextTick();
        expect(wrapper.vm.$data.newSelectedBranch).toBe("dev");
        expect(selectBranchMock.mock.calls.length).toBe(2);
        expect(selectBranchMock.mock.calls[1][1]).toBe("dev");
    });

    it("if selected git branch already exists in state, it is selected in the dropdown", () => {
        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: true
                },
                branches: ["master", "dev"],
                selectedBranch: "dev"
            })
        })
        expect(wrapper.vm.$data.newSelectedBranch).toBe("dev");
    });

    it("first git commit is selected automatically and selecting a git commit triggers select commit action", async () => {
        const selectCommitMock = jest.fn();
        const store = createStore({
            metadata: {
                git_supported: true
            },
            branches: ["master", "dev"]
        },
            {
                ...mockActions,
                SelectCommit: selectCommitMock
            })

        const wrapper = shallowMount(GitSelections, {
            store
        })
        await Vue.nextTick();
        wrapper.vm.$store.commit("git/SetCommits", commits);
        await Vue.nextTick();
        expect(wrapper.vm.$data.newSelectedCommitId).toBe("commit1");
        expect(selectCommitMock.mock.calls.length).toBe(1);
        expect(selectCommitMock.mock.calls[0][1]).toBe("commit1");
        wrapper.findAll("#git-commit option").at(1).setSelected();
        await Vue.nextTick();
        expect(wrapper.vm.$data.newSelectedCommitId).toBe("commit2");
        expect(selectCommitMock.mock.calls.length).toBe(2);
        expect(selectCommitMock.mock.calls[1][1]).toBe("commit2");
    });

    it("if selected git commit already exists in state, it is selected in the dropdown", async () => {
        const store = createStore({
            metadata: {
                git_supported: true
            },
            branches: ["master", "dev"],
            selectedCommit: "commit2"
        })

        const wrapper = shallowMount(GitSelections, {
            store
        })
        await Vue.nextTick();
        wrapper.vm.$store.commit("git/SetCommits", commits);
        await Vue.nextTick();
        expect(wrapper.vm.$data.newSelectedCommitId).toBe("commit2");
    });
})
