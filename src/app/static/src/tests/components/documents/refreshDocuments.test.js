import Vue from "vue";
import {mount, shallowMount} from "@vue/test-utils";
import refreshDocuments from "../../../js/components/documents/refreshDocuments";
import {mockAxios} from "../../mockAxios";

describe("refresh documents", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    const expectButtonRunningState = (wrapper) =>  {
        let button = wrapper.find("button");
        expect(button.attributes().disabled).toBe("disabled");
        expect(button.text()).toBe("...");
    };

    const expectButtonEnabledState = (wrapper) =>  {
        let button = wrapper.find("button");
        expect(button.attributes().disabled).toBeUndefined();
        expect(button.text()).toBe("Update");
    };

    it("button is disabled until url is provided", () => {
        const wrapper = shallowMount(refreshDocuments);
        expect(wrapper.find("button").attributes().disabled).toBe("disabled");
    });

    it("button is enabled once url is provided", async () => {
        const wrapper = shallowMount(refreshDocuments);
        wrapper.find("input").setValue("something");
        await Vue.nextTick();
        expectButtonEnabledState(wrapper);
    });

    it("can refresh documents", async () => {

        mockAxios.onPost("http://app/documents/refresh")
            .reply(200);

        const wrapper = shallowMount(refreshDocuments);
        wrapper.find("input").setValue("something");

        await Vue.nextTick();

        wrapper.find("button").trigger("click");

        await Vue.nextTick();

        expectButtonRunningState(wrapper);

        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);

        await Vue.nextTick();

        expect(wrapper.find(".text-success").text()).toBe("Documents have been updated!");
        expectButtonEnabledState(wrapper);
        expect(wrapper.emitted().refreshed.length).toBe(1);
    });

    it("shows error if refreshing fails", async () => {

        mockAxios.onPost("http://app/documents/refresh")
            .reply(500);

        const wrapper = mount(refreshDocuments);
        wrapper.find("input").setValue("something");

        await Vue.nextTick();

        wrapper.find("button").trigger("click");

        await Vue.nextTick();

        expectButtonRunningState(wrapper);

        await Vue.nextTick();

        expect(mockAxios.history.post.length).toBe(1);

        await Vue.nextTick();

        expect(wrapper.find(".text-danger").text()).toBe("Error: could not update documents");
        expectButtonEnabledState(wrapper);
    });

});
