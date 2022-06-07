import {shallowMount} from "@vue/test-utils";
import StoreErrorInfo from "../../js/components/storeErrorInfo.vue"

const errorMessage =  {code: "Error", message: "Error Alert"}

function getWrapper(error = errorMessage) {
    return shallowMount(StoreErrorInfo, {
        propsData: {
            error: error
        }
    });
}

describe("vuex StoreErrorInfo", () => {

    it("renders error correctly", async () => {
        const wrapper = getWrapper()

        expect(wrapper.find(".error-message").text()).toBe("Error Alert");
        expect(wrapper.find("div").classes()).toStrictEqual(["pt-1", "small", "text-danger"])
    });

    it("renders code value if message is not given", async () => {
        const errorMsg = {code: "Error", message: null}
        const wrapper = getWrapper(errorMsg)

        expect(wrapper.find(".error-message").text()).toBe("Error");
        expect(wrapper.find("div").classes()).toStrictEqual(["pt-1", "small", "text-danger"])
    });

});
