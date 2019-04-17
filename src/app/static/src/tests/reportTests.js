import {expect} from "chai";
import {describe} from "mocha";
import axios from "axios";
import {mount} from '@vue/test-utils'
import MockAdapter from "axios-mock-adapter";
import PublishSwitch from "../js/components/reports/publishSwitch.vue"

describe('report page', () => {

    describe("publishSwitch", () => {

        it('emits toggle event after successful publish toggle', (done) => {
            const mockAxios = new MockAdapter(axios);
            mockAxios.onPost('/v1/reports/name1/versions/version/publish/')
                .reply(200);

            const wrapper = mount(PublishSwitch, {
                propsData: {
                    report: {
                        name: "name1",
                        id: "version",
                        published: false
                    }
                }
            });

            wrapper.find('[data-toggle="toggle"]').trigger("click");

            setTimeout(() => {
                expect(mockAxios.history.post.length).to.eq(1);
                expect(wrapper.emitted().toggle).to.not.eq(undefined);
                done();
            });

        })
    });

});
