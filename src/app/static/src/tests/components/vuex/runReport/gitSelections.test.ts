import { shallowMount } from "@vue/test-utils";
import GitSelections from "../../../../js/components/vuex/runReport/gitSelections.vue";
import Vuex from "vuex";
import {ActionContext, ActionTree} from "vuex";
import { mockGitState, mockRunReportRootState, RecursivePartial } from "../../../mocks";
import { GitState } from "../../../../js/store/git/git";
import {GitAction} from "../../../../js/store/git/actions";
import { namespace } from "../../../../js/store/runReport/store";
// import {RunnerRootState} from "../../../utils/types";

describe("GitSelections", () => {

    const mockActions = {
        fetchMetadata: jest.fn(),
        selectBranch: jest.fn(),
        selectCommit: jest.fn()
    };

    // const createStore = (gitState: RecursivePartial<GitState>, actions: GitAction & ActionTree<GitState, RunnerRootState> = mockActions) => {
        const createStore = (gitState: RecursivePartial<GitState>) => {
        return new Vuex.Store({
            // state: mockRunReportRootState({
            //     git: mockGitState(gitState)
            // }),
            modules: {
                [namespace.git]: {
                    namespaced: true,
                    state: mockGitState(gitState),
                    // actions
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
})
