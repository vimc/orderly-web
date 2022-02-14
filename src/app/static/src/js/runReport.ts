import Vue from 'vue';
import $ from 'jquery';

import runReport from './components/runReport/runReport.vue'



$(document).ready(() => {
    if ($('#runReportVueApp').length > 0) {
        new Vue({
            el: '#runReportVueApp',
            components: {
                runReport: runReport
            }
        });
    }
});