import Vue from 'vue';
import $ from 'jquery';

import runWorkflowTabs from './components/runWorkflow/runWorkflowTabs.vue'

$(document).ready(() => {
    if ($('#runWorkflowTabsVueApp').length > 0) {
        new Vue({
            el: '#runWorkflowTabsVueApp',
            components: {
                runWorkflowTabs: runWorkflowTabs
            }
        });
    }
});