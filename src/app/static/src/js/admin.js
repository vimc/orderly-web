import Vue from 'vue'
import $ from 'jquery';
import manageRoles from './components/admin/manageRoles.vue'
import manageUsers from "./components/admin/manageUsers";
import manageRolePermissions from "./components/admin/manageRolePermissions.vue";

$(document).ready(() => {
    if ($('#adminVueApp').length > 0) {
        new Vue({
            el: '#adminVueApp',
            components: {
                manageRoles: manageRoles,
                manageUsers: manageUsers,
                manageRolePermissions: manageRolePermissions
            }
        });
    }
});