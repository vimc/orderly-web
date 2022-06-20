import {mockGitState, mockCommit} from "../../mocks";
import {GitMutation, mutations} from "../../../js/store/git/mutations";

describe("Git mutations", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    const state = mockGitState({
        branches: [],
        metadata: null,
        gitRefreshing: false
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

    it("sets commits and sets selectedCommit as first commit in array if not already selected", () => {
        const commits = [
            mockCommit({id: "first"}),
            mockCommit({id: "second"}),
        ]
        mutations[GitMutation.SetCommits](state, commits)
        expect(state.commits).toEqual(commits);
        expect(state.selectedCommit).toEqual("first");
    })

    it("sets commits and keeps existing selectedCommit if contained within the commits array", () => {
        const commits = [
            mockCommit({id: "first"}),
            mockCommit({id: "second"}),
        ]
        const state2 = mockGitState({
            selectedCommit: "second"
        })
        mutations[GitMutation.SetCommits](state2, commits)
        expect(state2.commits).toEqual(commits);
        expect(state2.selectedCommit).toEqual("second");
    })

    it("sets commits and set selectedCommit to empty if commits array is empty", () => {
        const commits = []
        const state2 = mockGitState({
            selectedCommit: "second"
        })
        mutations[GitMutation.SetCommits](state2, commits)
        expect(state2.commits).toEqual(commits);
        expect(state2.selectedCommit).toEqual("");
    })

    it("sets selectedCommit", () => {
        mutations[GitMutation.SelectCommit](state, "test")
        expect(state.selectedCommit).toEqual("test");
    })

    it("sets fetched git, setting branches as payload and wiping other state properties", () => {
        const branches = ["first", "second"]
        const state2 = mockGitState({
            branches: ["first"],
            selectedBranch: "first",
            commits: [
                mockCommit({id: "first"}),
            ],
            gitRefreshing: true
        })
        mutations[GitMutation.SetFetchedGit](state2, branches)
        expect(state2.branches).toEqual(branches);
        expect(state2.selectedBranch).toEqual("");
        expect(state2.commits).toEqual([]);
        expect(state2.selectedCommit).toEqual("");
        expect(state2.gitRefreshing).toEqual(false);
    })

    it("sets gitRefreshing", () => {
        mutations[GitMutation.SetGitRefreshing](state)
        expect(state.gitRefreshing).toEqual(true);
    })
});
