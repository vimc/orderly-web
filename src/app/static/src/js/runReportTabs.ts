import Vue from 'vue';
import $ from 'jquery';
import runReportTabs from "./components/runReportTabs/runReportTabs.vue";


$(document).ready(() => {
    if ($('#runReportTabsVueApp').length > 0) {
        new Vue({
            el: '#runReportTabsVueApp',
            components: {
                runReportTabs: runReportTabs
            }
        });
    }
});