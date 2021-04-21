import {reportVersionToLongTimestamp, longDate, longDateTime} from "../js/utils/helpers";

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

    describe("longDateTime", () => {
        it('returns expected long date time string', () => {
            const result = longDateTime(new Date(2019, 4, 15, 9, 10, 20));
            expect(result).toBe("Wed, 15 May 2019, 09:10");
        });
    });
});
