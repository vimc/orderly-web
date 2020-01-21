import Vue from 'vue'
import $ from 'jquery';
import manageRoles from './components/admin/manageRoles.vue'

$(document).ready(() => {
    if ($('#adminVueApp').length > 0) {
        new Vue({
            el: '#adminVueApp',
            components: {
                manageRoles: manageRoles
            },
            methods: {
            }
        });
    }
});