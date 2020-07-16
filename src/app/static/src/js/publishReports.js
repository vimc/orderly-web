import Vue from 'vue';
import $ from 'jquery';

import publishReports from './components/publishReports/publishReports.vue'

$(document).ready(() => {
    if ($('#publishReportsApp').length > 0) {
        new Vue({
            el: '#publishReportsApp',
            components: {
                publishReports
            }
        });
    }
});
