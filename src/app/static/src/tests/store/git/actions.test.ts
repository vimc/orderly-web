import {mockActionContext} from "../../mocks";
import {mockAxios} from "../../mockAxios";
import {actions, GitAction} from "../../../js/store/git/actions";
import {GitMutation} from "../../../js/store/git/mutations";

describe("Git actions", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    it("fetches metadata", async () => {
        mockAxios.onGet("http://app/report/run-metadata")
            .reply(200, {"data": "TEST"});
        const commit = jest.fn();
        await actions[GitAction.FetchMetadata](mockActionContext({commit}), {})
        expect(commit.mock.calls.length).toBe(1);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SetMetadata);
        expect(commit.mock.calls[0][1]).toBe("TEST")
    })

    it("selects branch and set commits", async () => {
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

    it("selects commit", async () => {
        const commit = jest.fn();
        await actions[GitAction.SelectCommit](mockActionContext({commit}), "testcommit")
        expect(commit.mock.calls.length).toBe(1);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SelectCommit);
        expect(commit.mock.calls[0][1]).toBe("testcommit")
    })
});
