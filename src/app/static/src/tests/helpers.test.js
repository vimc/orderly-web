import {reportVersionToLongTimestamp, longDate} from "../js/utils/helpers.ts";
import {workflowRunDetailsToMetadata} from "../js/utils/helpers";

describe('helpers', () => {
    describe('reportVersionToLongTimestamp', () => {
        it('returns expected long timestamp string', () => {
            const result = reportVersionToLongTimestamp("20190514-134604-97c74832");
            expect(result).toBe("Tue May 14 2019, 13:46")
        });

        it('raises error when input is incorrect format', () => {
            const expectedError = "Unable to parse 20190514X-134604-97c74832 as version identifier: Did not match regex"
            expect(() => reportVersionToLongTimestamp("20190514X-134604-97c74832")).toThrow(expectedError);
        });
    });

    describe('longDate', () => {
        it('returns expected long date string', () => {
            const result = longDate(new Date(2019, 4, 15));
            expect(result).toBe("Wed May 15 2019");
        });
    });

    it('workflowRunDetailsToMetadata', () => {
        const details = {
            "name": "My workflow",
            "key": "blissful_gavial",
            "email": "test.user@example.com",
            "date": "2021-08-11T08:37:23.135Z",
            "reports": [
                {
                    "workflow_key": "blissful_gavial",
                    "key": "nearsighted_titmouse",
                    "report": "minimal",
                    "params": {}
                },
                {
                    "workflow_key": "blissful_gavial",
                    "key": "palaeoclimatological_cicada",
                    "report": "other",
                    "params": {"nmin": "4"}
                }
             ],
            "instances": {"source": "UAT"},
            "git_branch": "master",
            "git_commit": "abc123"
        };
        const expectedMetadata = {
            name: "My workflow",
            reports: [
                {
                    name: "minimal",
                    params: {}
                },
                {
                    name: "other",
                    params: {nmin: "4"}
                }
            ],
            instances: {"source": "UAT"},
            git_branch: "master",
            git_commit: "abc123",
            changelog: null
        };
        expect(workflowRunDetailsToMetadata(details)).toStrictEqual(expectedMetadata);
    });
});
