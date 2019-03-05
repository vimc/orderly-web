import {expect} from "chai";
import {mapUser, vm} from "../js/admin";
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {describe} from "mocha";

// This sets the mock adapter on the default instance
const mock = new MockAdapter(axios);

describe('mapUser', () => {

    it('adds url to users', () => {
        const user = {username: "test.user", email: "test@test.com"};
        expect(mapUser(user).url).to.eq("/users/test.user")
    })
});

describe('addUser', () => {

    it('adds user to vm on success', (done) => {
        mock.onPost('/admin/adduser').reply(200);

        const newUser = "new.user";
        vm.newUser = newUser;
        vm.addUser();

        setTimeout(() => {
            expect(vm.users.map(u => u.username).indexOf(newUser)).to.be.above(-1);
            expect(vm.newUser).to.eq("");
            done();
        })
    });

    it('does not add user but adds error to vm on failure', (done) => {
        mock.onPost('/admin/adduser').reply(500);

        const badUser = "bad.user";
        vm.newUser = badUser;
        vm.addUser();

        setTimeout(() => {
            expect(vm.users.map(u => u.username).indexOf(badUser)).to.eq(-1);
            expect(vm.error).to.eq("An error occurred while adding a new user.");
            expect(vm.newUser).to.eq("");
            done();
        })
    });

});