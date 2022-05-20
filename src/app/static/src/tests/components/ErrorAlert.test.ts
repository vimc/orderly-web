import {shallowMount} from "@vue/test-utils";
import ErrorAlert from "../../js/components/ErrorAlert.vue"

const errorMessage =  {code: "Error", message: "Error Alert"}

function getWrapper(error = errorMessage) {
    return shallowMount(ErrorAlert, {
        propsData: {
            error: error
        }
    });
}

describe("vuex errorAlert", () => {

    it("renders error Alert correctly", async () => {
        const wrapper = getWrapper()

        expect(wrapper.find(".error-message").text()).toBe("Error Alert");
        expect(wrapper.find("div").classes()).toStrictEqual(["pt-1", "text-danger"])
    });

    it("renders code value if message is not given", async () => {
        const errorMsg = {code: "Error", message: null}
        const wrapper = getWrapper(errorMsg)

        expect(wrapper.find(".error-message").text()).toBe("Error");
        expect(wrapper.find("div").classes()).toStrictEqual(["pt-1", "text-danger"])
    });

});
