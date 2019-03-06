import {vm} from "../js/report";
import {expect} from "chai";
import {describe} from "mocha";
import {mockAxios} from "./setup";

describe('report page', () => {

    it('publishes report', () => {
        mockAxios.onPost('/v1/reports/name1/versions/version/publish').reply(200);

        vm.name = "name1";
        vm.id = "v1";

        vm.publish();

        setTimeout(() => {
            expect(vm.published).to.be.true;
            done();
        });

    })
});
