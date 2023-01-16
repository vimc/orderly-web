import {api} from "../../js/utils/apiService";
import {mockFailure, mockRunReportRootState, mockSuccess} from "../mocks";
import {mockAxios} from "../mockAxios"

const rootState = mockRunReportRootState();

describe("ApiService", () => {

    beforeEach(() => {
        console.log = jest.fn();
        console.warn = jest.fn();
        mockAxios.reset();
    });

    afterEach(() => {
        (console.log as jest.Mock).mockClear();
        (console.warn as jest.Mock).mockClear();
        jest.clearAllMocks();
    });

    it("commits error with the specified type", async () => {

        mockAxios.onGet(`/http://app/reports`)
            .reply(500, mockFailure( "some error"));

        let committedType: any = false;
        let committedPayload: any = false;

        const commit = ({type, payload}: any) => {
            committedType = type;
            committedPayload = payload;
        };

        await api({commit, rootState} as any)
            .withError("TEST_TYPE")
            .get("/reports");

        expect(committedType).toBe("TEST_TYPE");
        expect(committedPayload["detail"]).toBe("some error");
    });

    it("throws could not parse error if API response errors are empty", async () => {
        mockAxios.onGet(`/http://app/reports`)
            .reply(500, {data: {}, status: "failure", errors: []});

        await expectCouldNotParseAPIResponseError();
    });

    it("throws parse error if API response errors are missing", async () => {
        mockAxios.onGet(`/http://app/reports`)
            .reply(500, {data: {}, status: "failure"});

        await expectCouldNotParseAPIResponseError();
    });

    it("commits the success response with the specified type", async () => {

        mockAxios.onGet(`/http://app/reports`)
            .reply(200, mockSuccess(true));

        let committedType: any = false;
        let committedPayload: any = false;
        const commit = ({type, payload}: any) => {
            committedType = type;
            committedPayload = payload;
        };

        await api({commit, rootState} as any)
            .withSuccess("TEST_TYPE")
            .get("/reports");

        expect(committedType).toBe("TEST_TYPE");
        expect(committedPayload).toBe(true);
    });

    it("commits the success response with the specified type with root options", async () => {

        mockAxios.onGet(`/http://app/reports/`)
            .reply(200, mockSuccess(true));

        let committedType: any = false;
        let committedPayload: any = false;
        const commit = ({type, payload}: any) => {
            committedType = type;
            committedPayload = payload;
        };

        await api({commit, rootState} as any)
            .withSuccess("TEST_TYPE")
            .get("/reports/");

        expect(committedType).toBe("TEST_TYPE");
        expect(committedPayload).toBe(true);
    });

    it("returns the response object", async () => {

        mockAxios.onGet(`/http://app/reports`)
            .reply(200, mockSuccess("TEST"));

        const commit = jest.fn();
        const response = await api({commit, rootState} as any)
            .withSuccess("TEST_TYPE")
            .get("/reports");

        expect(response).toStrictEqual({data: "TEST", errors: [], status: "success"});
    });

    it("does nothing on error if ignoreErrors is true", async () => {

        mockAxios.onGet(`/run-report/`)
            .reply(500, "some error message");

        await api({commit: jest.fn(), rootState} as any)
            .withSuccess("whatever")
            .ignoreError()
            .get("/run-report/");

        expect((console.warn as jest.Mock).mock.calls.length).toBe(0);
    });

    it("passes language header on get", async () => {

        mockAxios.onGet(`/run-report/`)
            .reply(200, mockSuccess(true));

        await api({commit: jest.fn(), rootState} as any)
            .withSuccess("whatever")
            .ignoreError()
            .get("/run-report/");

        expect(mockAxios.history.get[0].headers).toStrictEqual({
            "Accept": "application/json, text/plain, */*",
            "Accept-Language": "en-GB"
        })
    });

    it("passes language header on post", async () => {

        mockAxios.onPost(`/run-report/`)
            .reply(200, mockSuccess(true));

        await api({commit: jest.fn(), rootState} as any)
            .withSuccess("whatever")
            .ignoreError()
            .postAndReturn("/run-report/", {});

        expect(mockAxios.history.post[0].headers).toStrictEqual({
            "Accept": "application/json, text/plain, */*",
            "Accept-Language": "en-GB",
            "Content-Type": "application/json"
        })
    });

});

async function expectCouldNotParseAPIResponseError() {
    const commit = jest.fn();
    await api({commit, rootState} as any)
        .get("/reports");

    expect(commit.mock.calls.length).toBe(1);
    expect(commit.mock.calls[0][0]).toStrictEqual({
        type: `errors/ErrorAdded`,
        payload: {
            error: "MALFORMED_RESPONSE",
            detail: "Could not parse API response. Please contact support."
        }
    });
    expect(commit.mock.calls[0][1]).toStrictEqual({root: true});
}
