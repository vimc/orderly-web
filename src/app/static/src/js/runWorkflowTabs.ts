import Vue from 'vue';
import {mapActions} from "vuex";
import $ from 'jquery';

import runWorkflowTabs from './components/runWorkflow/runWorkflowTabs.vue'
import {store} from "./store/runWorkflow/store";
import {GitAction} from "./store/git/actions";

$(document).ready(() => {
    if ($('#runWorkflowTabsVueApp').length > 0) {
        new Vue({
            el: '#runWorkflowTabsVueApp',
            store,
            components: {
                runWorkflowTabs: runWorkflowTabs
            },
            beforeMount: function () {
                this.fetchMetadata()
            },
            methods: {
                ...mapActions({fetchMetadata: `git/${GitAction.FetchMetadata}`}),
            }
        });
    }
});
