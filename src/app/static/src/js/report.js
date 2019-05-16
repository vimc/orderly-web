import Vue from 'vue';

import publishSwitch from './components/reports/publishSwitch.vue'
import runReport from './components/reports/runReport.vue'

export const bootstrap = require("bootstrap");

let data = {report: null};

// report var should be set externally in the browser
if (typeof report !== "undefined") {
    data.report = report;
}

export const publishVm = new Vue({
    el: '#publishSwitchVueApp',
    data: data,
    components: {
        publishSwitch: publishSwitch
    },
    methods: {
        handleToggle: function() {
            this.report.published = !this.report.published
        }
    }
});

export const runVm  = new Vue({
    el: '#runReportVueApp',
    data: data,
    components: {
        runReport: runReport
    }
});