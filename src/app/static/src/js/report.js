import Vue from 'vue';
import $ from 'jquery';

import publishSwitch from './components/reports/publishSwitch.vue'
import runReportInline from './components/reports/runReportInline.vue'
import globalReportReadersList from './components/reports/globalReportReadersList'
import reportReadersList from './components/reports/reportReadersList.vue'
import globalReaderRolesList from './components/reports/globalReportReaderRolesList.vue'
import scopedReaderRolesList from './components/reports/scopedReportReaderRolesList.vue'
import editIcon from './components/reports/editIcon.vue'
import reportTags from './components/reports/reportTags'
import reportDependencies from './components/reports/reportDependencies'

let data = {report: null};
let report;

// report var should be set externally in the browser
if (typeof report !== "undefined") {
    data.report = report;
}

$(document).ready(() => {
    if ($('#publishSwitchVueApp').length > 0) {
        new Vue({
            el: '#publishSwitchVueApp',
            components: {
                publishSwitch: publishSwitch
            },
            data: data,
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
            components: {
                runReportInline
            },
            data: data
        });
    }
    if ($('#reportReadersListVueApp').length > 0) {
        new Vue({
            el: '#reportReadersListVueApp',
            components: {
                editIcon: editIcon,
                reportReadersList: reportReadersList,
                globalReportReadersList: globalReportReadersList,
                globalReaderRolesList: globalReaderRolesList,
                scopedReaderRolesList: scopedReaderRolesList
            },
            data: data
        });
    }

    new Vue({
        el: '#reportTagsVueApp',
        components: {
            reportTags: reportTags,
        },
        data: data
    });

    new Vue({
        el: '#reportDependenciesVueApp',
        components: {
            reportDependencies: reportDependencies
        },
        data: data
    });

    $('[data-toggle="tooltip"]').tooltip();

    if (location.hash) {
        $('a[href="' + location.hash + '-tab"]').tab("show");
    }

    $('a[data-toggle="tab"]').on("click", function () {
        const hash = $(this).attr("href");
        location.hash = hash.split("#")[1].split("-")[0];
    });
});
