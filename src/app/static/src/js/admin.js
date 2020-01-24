import Vue from 'vue'
import $ from 'jquery';
import admin from './components/admin/admin.vue'

$(document).ready(() => {
    if ($('#adminVueApp').length > 0) {
        new Vue({
            el: '#adminVueApp',
            components: {
                admin: admin
            },
            methods: {
            }
        });
    }
});