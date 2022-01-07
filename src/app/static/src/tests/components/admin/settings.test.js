import {shallowMount} from '@vue/test-utils';
import {mockAxios} from "../../mockAxios";
import Settings from "../../../js/components/admin/settings.vue";
import ErrorInfo from "../../../js/components/errorInfo";

describe("settings", () => {

    const url =' http://app/settings/auth-allow-guest/';
    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet(url)
            .reply(200, {"data": true});
    });

    it('gets allow guest setting and renders value', async () => {
        const wrapper = shallowMount(Settings);
        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(mockAxios.history.get[0].url).toBe(url);
            expect(wrapper.find("input").attr("checked")).toBe(true);
            expect(wrapper.findComponent(ErrorInfo).props().error).toBe(null);
            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("");
            done();
        });
    });

    it('sets allow guest setting when value changes', () => {
        mockAxios.onPost(url)
            .reply(200);
        const wrapper = shallowMount(Settings);
        wrapper.setData({authAllowGuest: false});
        wrapper.vm.setAuthAllowGuest();
        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.post[0].url).toBe(url);
            expect(mockAxios.history.post[0].data).toBe(false);
            expect(wrapper.findComponent(ErrorInfo).props().error).toBe(null);
            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("");
            done();
        });
    });

    it('shows error from fetching setting', () => {
        mockAxios.onGet(url)
            .reply(500, "TEST ERROR");
        const wrapper = shallowMount(Settings);
        setTimeout(() => {
            expect(wrapper.findComponent(ErrorInfo).props().error.response.data).toBe("TEST ERROR");
            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("could not get allow guest user");
            done();
        });
    });

    it('shows error from posting setting', () => {
        mockAxios.onPost(url)
            .reply(500, "TEST ERROR");
        const wrapper = shallowMount(Settings);
        wrapper.vm.setAuthAllowGuest();
        setTimeout(() => {
            expect(wrapper.findComponent(ErrorInfo).props().error.response.data).toBe("TEST ERROR");
            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("could not get allow guest user");
            done();
        });
    });
});