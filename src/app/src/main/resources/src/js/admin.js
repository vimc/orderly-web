import Vue from "vue";
import axios from "axios";

const mapUser = (user) => {
    return {...user, url: "/users/" + user.username}
};

const data = {
    users: users.map(mapUser),
    newUser: "",
    error: ""
};

const addUser = (username) => {
    axios.post('/admin/adduser', username).then(() => {
        data.users.push(mapUser({username: username, email: username + "@gmail.com"}));
        data.newUser = ""
    }).catch(() => {
        data.error = "An error occured while adding a new user."
    });
};

const vm = new Vue({
    el: '#vueApp',
    data: data,
    methods: {
        addUser: () => addUser(data.newUser)
    }
});
