import Vue from 'vue';
import $ from 'jquery';

import reportLog from './components/reportLog/reportLog.vue'

$(document).ready(() => {
    if ($('#reportLogVueApp').length > 0) {
        new Vue({
            el: '#reportLogVueApp',
            components: {
                reportLog: reportLog
            }
        });
    }
});