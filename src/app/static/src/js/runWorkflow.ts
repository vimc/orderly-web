import Vue from 'vue';
import $ from 'jquery';

import runWorkflow from './components/runWorkflow/runWorkflow.vue'

$(document).ready(() => {
    if ($('#runWorkflowVueApp').length > 0) {
        new Vue({
            el: '#runWorkflowVueApp',
            components: {
                runWorkflow: runWorkflow
            }
        });
    }
});