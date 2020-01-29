import Vue from 'vue'
import $ from 'jquery';
import manageRoles from './components/admin/manageRoles.vue'
import manageUsers from "./components/admin/manageUsers";

$(document).ready(() => {
    if ($('#adminVueApp').length > 0) {
        new Vue({
            el: '#adminVueApp',
            components: {
                manageRoles: manageRoles,
                manageUsers: manageUsers
            }
        });
    }
});