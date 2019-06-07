import {api} from "../js/utils/api";

describe('api', () => {
    describe('errorMessage', () => {
        it('getsErrorMessage from response', () => {
            const testResponse = {"data": {"errors": [{"message": "test message"}]}}
            expect(api.errorMessage(testResponse)).toBe("test message");
        });

        it('returns falsey if error message cannot be found in response', () => {
            expect(api.errorMessage(undefined)).toBeFalsy();
            expect(api.errorMessage({})).toBeFalsy();
            expect(api.errorMessage({"data": {}})).toBeFalsy();
            expect(api.errorMessage({"data": {"errors": {}}})).toBeFalsy();
            expect(api.errorMessage({"data": {"errors": []}})).toBeFalsy();
            expect(api.errorMessage({"data": {"errors": [{}]}})).toBeFalsy();
        });
    });
});