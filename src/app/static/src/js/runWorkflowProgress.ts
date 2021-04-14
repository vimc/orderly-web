import Vue from 'vue';
import $ from 'jquery';

import runWorkflowProgress from './components/runWorkflow/runWorkflowProgress.vue'

$(document).ready(() => {
    if ($('#runWorkflowProgressVueApp').length > 0) {
        new Vue({
            el: '#runWorkflowProgressVueApp',
            components: {
                runWorkflowProgress: runWorkflowProgress
            }
        });
    }
});