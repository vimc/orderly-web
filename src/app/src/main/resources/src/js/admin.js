import Vue from "vue";
import axios from "axios";

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
    newUser: "",
    error: ""
};

const addUser = (username) => {
    axios.post('/admin/adduser', username).then(() => {
        data.users.push(mapUser({username: username, email: username + "@gmail.com"}));
        data.newUser = ""
    }).catch(() => {
        data.error = "An error occurred while adding a new user.";
        data.newUser = "";
    });
};

export const vm = new Vue({
    el: '#vueApp',
    data: data,
    methods: {
        addUser: () => addUser(data.newUser)
    }
});
