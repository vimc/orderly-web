import {mount, shallowMount} from '@vue/test-utils';
import ErrorInfo from "../../js/components/errorInfo.vue"

describe("errorInfo", () => {

    it('shows error message if it exists', () => {

        const wrapper = mount(ErrorInfo, {
            propsData: {
                apiError: {
                    response: {data: {"errors": [{"message": "test error message"}]}}
                },
                defaultMessage: "test default"
            }
        });

        expect(wrapper.find('.text-danger').text()).toBe("Error: test error message");
    });

    it('shows default message if error is in the wrong format', () => {

        const wrapper = mount(ErrorInfo, {
            propsData: {
                apiError: {
                    response: {data: "something went wrong"}
                },
                defaultMessage: "test default"
            }
        });

        expect(wrapper.find('.text-danger').text()).toBe("Error: test default");
    });

    it("uses default message if error message cannot be found in response", () => {

        const wrapper = shallowMount(ErrorInfo);

        expect(wrapper.vm.apiErrorMessage({})).toBeFalsy();
        expect(wrapper.vm.apiErrorMessage({"data": {}})).toBeFalsy();
        expect(wrapper.vm.apiErrorMessage({"data": {"errors": {}}})).toBeFalsy();
        expect(wrapper.vm.apiErrorMessage({"data": {"errors": []}})).toBeFalsy();
        expect(wrapper.vm.apiErrorMessage({"data": {"errors": [{}]}})).toBeFalsy();

    });

    it('shows nothing if error is null', () => {

        const wrapper = mount(ErrorInfo, {
            propsData: {
                apiError: null,
                defaultMessage: "test default"
            }
        });

        expect(wrapper.findAll('.text-danger').length).toBe(0);
    });

});