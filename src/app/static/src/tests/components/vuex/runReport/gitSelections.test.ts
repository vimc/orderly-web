import {shallowMount} from "@vue/test-utils";
import GitSelections from "../../../../js/components/vuex/runReport/gitSelections.vue";
import Vuex from "vuex";
import {mockGitState, mockRunReportMetadata, mockRunReportRootState, RecursivePartial} from "../../../mocks";
import {GitState} from "../../../../js/store/git/git";

describe("GitSelections", () => {

    const createStore = (gitState: RecursivePartial<GitState>) => {
        return new Vuex.Store({
            state: mockRunReportRootState({
                git: mockGitState(gitState)
            })
        })
    }

    it("renders git branch drop down if git supported", () => {

        const wrapper = shallowMount(GitSelections, {
            store: createStore({
                metadata: {
                    git_supported: true
                },
                git_branches: ["master", "dev"]
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
                git_branches: ["master", "dev"]
            })
        })

        expect(wrapper.find("#git-branch-form-group").exists()).toBe(false);
    });
})
