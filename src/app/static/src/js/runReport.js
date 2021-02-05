import Vue from 'vue';
import $ from 'jquery';

import runReport from './components/runReport/runReport.vue'
import reportLogs from "./components/reportLogs/reportLogs";

$(document).ready(() => {
    if ($('#runReportVueApp').length > 0) {
        new Vue({
            el: '#runReportVueApp',
            components: {
                runReport: runReport
            }
        });
    }
    if ($('#reportLogsVueApp').length > 0) {
        new Vue({
            el: '#reportLogsVueApp',
            components: {
                reportLogs
            }
        });
    }
});