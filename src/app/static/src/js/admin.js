import Vue from 'vue'
import $ from 'jquery';
import adminApp from "./components/admin/adminApp.vue";

$(document).ready(() => {
    if ($('#adminVueApp').length > 0) {
        new Vue({
            el: '#adminVueApp',
            components: {adminApp}
        });
    }
});