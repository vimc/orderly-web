import {mockGitState} from "../../mocks";
import {GitMutation, mutations} from "../../../js/store/git/mutations";

describe("Git mutations", () => {

    it("sets metadata", () => {
        const state = mockGitState({
            gitBranches: [],
            metadata: null
        });
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
        expect(state.gitBranches).toEqual(["dev", "main"]);
        expect(state.metadata).toEqual({
            git_supported: true,
            instances_supported: true,
            instances: {"inst": ["1", "2"]},
            changelog_types: ["public"]
        });
    })
});
