import Vue from "vue";

import newUserForm from './components/admin/newUserForm.vue'

export const mapUser = (user) => {
    return {...user, url: "/users/" + user.username}
};

let mappedUsers = [];

// users var should be set externally in the browser
if (typeof users !== "undefined") {
    mappedUsers = users.map(mapUser);
}

const data = {
    users: mappedUsers,
    error: ""
};

export const vm = new Vue({
    el: '#vueApp',
    data: data,
    components: {
        newUserForm: newUserForm
    },
    methods: {
        handleCreate(user) {
            data.users.push(mapUser(user));
        }
    }
});

