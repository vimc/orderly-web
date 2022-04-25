import {mockGitState, mockCommit} from "../../mocks";
import {GitMutation, mutations} from "../../../js/store/git/mutations";

describe("Git mutations", () => {
    const state = mockGitState({
        branches: [],
        metadata: null
    });

    it("sets metadata", () => {
        mutations[GitMutation.SetMetadata](
            state,
            {
                metadata: {
                    git_supported: true,
                    instances_supported: true,
                    instances: {"inst": ["1", "2"]},
                    changelog_types: ["public"]
                },
                git_branches: ["dev", "main"]
            })
        expect(state.branches).toEqual(["dev", "main"]);
        expect(state.metadata).toEqual({
            git_supported: true,
            instances_supported: true,
            instances: {"inst": ["1", "2"]},
            changelog_types: ["public"]
        });
    })

    it("sets selectedBranch", () => {
        mutations[GitMutation.SelectBranch](state, "test")
        expect(state.selectedBranch).toEqual("test");
    })

    it("sets commits", () => {
        const commits = [
            mockCommit()
        ]
        mutations[GitMutation.SetCommits](state, commits)
        expect(state.commits).toEqual(commits);
    })

    it("sets selectedCommit", () => {
        mutations[GitMutation.SelectCommit](state, "test")
        expect(state.selectedCommit).toEqual("test");
    })
});
