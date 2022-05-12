import {mockActionContext, mockGitState} from "../../mocks";
import {mockAxios} from "../../mockAxios";
import {actions, GitAction} from "../../../js/store/git/actions";
import {GitMutation} from "../../../js/store/git/mutations";

describe("Git actions", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    it("FetchMetadata action fetches metadata and dispatches ManageUpdatedBranches action", async () => {
        mockAxios.onGet("http://app/report/run-metadata")
            .reply(200, {"data": "TEST"});
        const commit = jest.fn();
        const dispatch = jest.fn();
        const state = mockGitState()
        await actions[GitAction.FetchMetadata](mockActionContext({commit, dispatch, state}), {})
        expect(commit.mock.calls.length).toBe(1);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SetMetadata);
        expect(commit.mock.calls[0][1]).toBe("TEST")
        expect(dispatch.mock.calls.length).toBe(1);
        expect(dispatch.mock.calls[0][0]).toBe(GitAction.ManageUpdatedBranches);
    })

    it("ManageUpdatedBranches dispatches SelectBranch action for first branch in array if no branch selected", async () => {
        const commit = jest.fn();
        const dispatch = jest.fn();
        const state = mockGitState({
            branches: ["branch1", "branch2"],
            selectedBranch: ""
        })
        await actions[GitAction.ManageUpdatedBranches](mockActionContext({commit, dispatch, state}), {})
        expect(dispatch.mock.calls.length).toBe(1);
        expect(dispatch.mock.calls[0][0]).toBe(GitAction.SelectBranch);
        expect(dispatch.mock.calls.length).toBe(1);
        expect(dispatch.mock.calls[0][1]).toBe("branch1")
    })

    it("ManageUpdatedBranches sets selectedBranch to empty if no branches are set in state", async () => {
        const commit = jest.fn();
        const dispatch = jest.fn();
        const state = mockGitState({
            branches: [],
            selectedBranch: "branch1"
        })
        await actions[GitAction.ManageUpdatedBranches](mockActionContext({commit, dispatch, state}), {})
        expect(dispatch.mock.calls.length).toBe(1);
        expect(dispatch.mock.calls[0][0]).toBe(GitAction.SelectBranch);
        expect(dispatch.mock.calls[0][1]).toBe("")
    })

    it("ManageUpdatedBranches does not dispatch SelectBranch action if selectedBranch has not changed", async () => {
        const commit = jest.fn();
        const dispatch = jest.fn();
        const state = mockGitState({
            branches: ["branch1", "branch2"],
            selectedBranch: "branch1"
        })
        await actions[GitAction.ManageUpdatedBranches](mockActionContext({commit, dispatch, state}), {})
        expect(dispatch.mock.calls.length).toBe(0);
    })

    it("SelectBranch action selects branch and set commits", async () => {
        mockAxios.onGet("http://app/git/branch/testbranch/commits/")
            .reply(200, {"data": "TEST"});
        const commit = jest.fn();
        await actions[GitAction.SelectBranch](mockActionContext({commit}), "testbranch")
        expect(commit.mock.calls.length).toBe(2);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SelectBranch);
        expect(commit.mock.calls[0][1]).toBe("testbranch")
        expect(commit.mock.calls[1][0]).toBe(GitMutation.SetCommits);
        expect(commit.mock.calls[1][1]).toBe("TEST")
    })

    it("SelectBranch action selects branch to empty and does not set commits if branch is not selected", async () => {
        mockAxios.onGet("http://app/git/branch/testbranch/commits/")
            .reply(200, {"data": "TEST"});
        const commit = jest.fn();
        await actions[GitAction.SelectBranch](mockActionContext({commit}), "")
        expect(commit.mock.calls.length).toBe(1);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SelectBranch);
        expect(commit.mock.calls[0][1]).toBe("")
    })

    it("RefreshGit sets gitRefreshing, commits SetFetchedGit mutation, and dispatches ManageUpdatedBranches action", async () => {
        mockAxios.onGet("http://app/git/fetch/")
            .reply(200, {"data": [{name: "master"}, {name: "other"}]});
        const commit = jest.fn();
        const dispatch = jest.fn();
        await actions[GitAction.RefreshGit](mockActionContext({commit, dispatch}), {})
        expect(commit.mock.calls.length).toBe(2);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SetGitRefreshing);
        expect(commit.mock.calls[1][0]).toBe(GitMutation.SetFetchedGit);
        expect(commit.mock.calls[1][1]).toStrictEqual(["master", "other"])
        expect(dispatch.mock.calls.length).toBe(1);
        expect(dispatch.mock.calls[0][0]).toBe(GitAction.ManageUpdatedBranches);
    })
});
