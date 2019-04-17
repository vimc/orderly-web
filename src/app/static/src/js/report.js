import Vue from 'vue';

import publishSwitch from './components/reports/publishSwitch.vue'

let data = {report: null};

// report var should be set externally in the browser
if (typeof report !== "undefined") {
    data.report = report;
}

export const vm = new Vue({
    el: '#vueApp',
    data: data,
    components: {
        publishSwitch: publishSwitch
    },
    methods: {
        switchTab: function (tabName) {
            this.tab = tabName
        },
        handleToggle: function() {
            this.report.published = !this.report.published
        }
    }
});