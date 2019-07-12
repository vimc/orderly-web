import {mount} from '@vue/test-utils';
import AddPermission from "../../../js/components/permissions/addPermission.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import {mockAxios} from "../../mockAxios";

describe("addPermission", () => {

    const availableUserGroups = [
        "testGroup1",
        "testGroup2"
    ];

    beforeEach(() => {
        mockAxios.reset();
    });

    function expectPostDataCorrect() {
        const postData = JSON.parse(mockAxios.history.post[0].data);
        expect(postData.name).toBe("reports.read");
        expect(postData.action).toBe("add");
        expect(postData.scope_prefix).toBe("report");
        expect(postData.scope_id).toBe("report1");
    }

    function createSut(type) {
        return mount(AddPermission, {
            propsData: {
                permission: {
                    name: "reports.read",
                    scope_prefix: "report",
                    scope_id: "report1"
                },
                availableUserGroups: availableUserGroups,
                type: type
            }
        });
    }

    it('add sets error and does not emit added event', (done) => {
        const testError = {test: "something"};
        mockAxios.onPost(`http://app/user-groups/testGroup1/actions/associate-permission/`)
            .reply(500, testError);

        const wrapper = createSut("user");

        wrapper.find('input').setValue('testGroup1');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);

            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("could not add user");
            expect(wrapper.find(ErrorInfo).props().apiError.response.data).toStrictEqual(testError);
            expect(wrapper.emitted().added).toBeUndefined();
            done();
        });
    });

    it('add calls associate permission endpoint and emits added event', (done) => {

        mockAxios.onPost(`http://app/user-groups/testGroup1/actions/associate-permission/`)
            .reply(200);

        const wrapper = createSut("user");

        wrapper.find("input").setValue('testGroup1');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(wrapper.findAll('.text-danger').length).toBe(0);
            expect(mockAxios.history.post.length).toBe(1);
            expectPostDataCorrect();
            expect(wrapper.vm.$data["newUserGroup"]).toBe("");

            done();
        });
    });

    describe("add permission to user", () => {

        it('renders', () => {

            const wrapper = createSut("user");
            wrapper.setData({
                error: "test error",
                defaultMessage: "default error"
            });

            expect(wrapper.find('input').attributes("placeholder")).toBe("email");
            expect(wrapper.find('button').text()).toBe("Add user");

            expect(wrapper.find(ErrorInfo).props().apiError).toBe("test error");
            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("default error");

        });

        it('validates that email value is an available user group', (done) => {

            const wrapper = createSut("user");

            wrapper.find('input').setValue('badUserGroup');
            wrapper.find('button').trigger('click');

            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(0);
                expect(wrapper.find('.text-danger').text()).toBe("Error: you must enter a valid email");
                done();
            });
        });
    });

    describe("add permission to role", () => {

        it('renders', () => {

            const wrapper = createSut("role");
            wrapper.setData({
                error: "test error",
                defaultMessage: "default error"
            });

            expect(wrapper.find('input').attributes("placeholder")).toBe("role name");
            expect(wrapper.find('button').text()).toBe("Add role");

            expect(wrapper.find(ErrorInfo).props().apiError).toBe("test error");
            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("default error");

        });

        it('validates that email value is an available user group', (done) => {

            const wrapper = createSut("role");

            wrapper.find('input').setValue('badUserGroup');
            wrapper.find('button').trigger('click');

            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(0);
                expect(wrapper.find('.text-danger').text()).toBe("Error: you must enter a valid role name");
                done();
            });
        });
    })

});