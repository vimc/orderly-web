import Vue from 'vue';
import $ from 'jquery';

import publishSwitch from './components/reports/publishSwitch.vue'
import runReport from './components/reports/runReport.vue'
import globalReportReadersList from './components/reports/globalReportReadersList'
import reportReadersList from './components/reports/reportReadersList.vue'
import globalReaderRolesList from './components/reports/globalReportReaderRolesList.vue'
import scopedReaderRolesList from './components/reports/scopedReportReaderRolesList.vue'
import editIcon from './components/reports/editIcon.vue'

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
                handleToggle: function () {
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
    if ($('#reportReadersListVueApp').length > 0) {
        new Vue({
            el: '#reportReadersListVueApp',
            data: data,
            components: {
                editIcon: editIcon,
                reportReadersList: reportReadersList,
                globalReportReadersList: globalReportReadersList,
                globalReaderRolesList: globalReaderRolesList,
                scopedReaderRolesList: scopedReaderRolesList
            }
        });
    }

    $('[data-toggle="tooltip"]').tooltip();

    if (location.hash) {
        $('a[href="' + location.hash + '-tab"]').tab("show");
    }

    $('a[data-toggle="tab"]').on("click", function () {
        const hash = $(this).attr("href");
        location.hash = hash.split("#")[1].split("-")[0];
    });
});