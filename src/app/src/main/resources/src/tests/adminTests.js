import {describe} from "mocha";
import {expect} from "chai";
import axios from "axios";
import {mount} from '@vue/test-utils'
import MockAdapter from "axios-mock-adapter";
import NewUserForm from '../js/components/admin/newUserForm.vue'

describe('mapUser', () => {

    // it('adds url to users', () => {
    //     const user = {username: "test.user", email: "test@test.com"};
    //     expect(mapUser(user).url).to.eq("/users/test.user")
    // })
});

describe('addUserForm', () => {

    it('sets default data', () => {
        const defaultData = NewUserForm.data();
        expect(defaultData.error).to.eq("");
        expect(defaultData.username).to.eq("");
        expect(defaultData.email).to.eq("");
    });

    it('emits created event on successful user creation', (done) => {
        const mockAxios = new MockAdapter(axios);
        mockAxios.onPost('/admin/adduser', "")
            .reply(200, {});

        const wrapper = mount(NewUserForm);
        wrapper.find('.submit').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).to.eq(1);
            expect(wrapper.emitted().created).to.deep.include([{username: "", email: ""}]);
            done()
        });
    });

    it('shows error on user creation failure', (done) => {
        const mockAxios = new MockAdapter(axios);
        mockAxios.onPost('/admin/adduser', "")
            .reply(500, {});

        const wrapper = mount(NewUserForm);
        wrapper.find('.submit').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).to.eq(1);
            expect(wrapper.emitted().created).to.eq(undefined);
            done()
        });
    });

});