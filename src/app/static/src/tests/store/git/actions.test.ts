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
        await actions[GitAction.FetchMetadata](mockActionContext({commit}))
        expect(commit.mock.calls.length).toBe(1);
        expect(commit.mock.calls[0][0]).toBe(GitMutation.SetMetadata);
        expect(commit.mock.calls[0][1]).toBe("TEST")
    })
});
