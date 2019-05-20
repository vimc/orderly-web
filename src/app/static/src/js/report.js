import Vue from 'vue';
import $ from 'jquery';

import publishSwitch from './components/reports/publishSwitch.vue'
import runReport from './components/reports/runReport.vue'

export const bootstrap = require("bootstrap");

let data = {report: null};

// report var should be set externally in the browser
if (typeof report !== "undefined") {
    data.report = report;
}

$(document).ready(() => {
    if ($('#publishSwitchVueApp').length > 0) {
        new Vue({
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
    }
    if ($('#runReportVueApp').length > 0) {
        new Vue({
            el: '#runReportVueApp',
            data: data,
            components: {
                runReport: runReport
            }
        });
    }
});
