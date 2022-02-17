import Vue from 'vue';
import {mapActions} from "vuex";
import $ from 'jquery';

import runReportTabs from './components/runReport/runReportTabs.vue'
import {store} from "./store/runReport/store";
import {GitAction} from "./store/git/actions";

$(document).ready(() => {
    if ($('#runReportTabsVueApp').length > 0) {
        new Vue({
            el: '#runReportTabsVueApp',
            store,
            components: {
                runReportTabs: runReportTabs
            },
            methods: {
                ...mapActions({fetchMetadata: `git/${GitAction.FetchMetadata}`}),
            },
            beforeMount: function () {
                this.fetchMetadata()
            }
        });
    }
});