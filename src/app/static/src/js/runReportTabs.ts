import Vue from 'vue';
import {mapActions} from "vuex";
import $ from 'jquery';

import runReportTabs from './components/runReport/runReportTabs.vue'
import {store} from "./store/runReport/store";
import {GitAction} from "./store/git/actions";

declare const initialReportName;
$(document).ready(() => {
    if ($('#runReportTabsVueApp').length > 0) {
        new Vue({
            el: '#runReportTabsVueApp',
            store,
            components: {
                runReportTabs: runReportTabs
            },
            beforeMount: function () {
                this.fetchMetadata()
            },
            methods: {
                ...mapActions({fetchMetadata: `git/${GitAction.FetchMetadata}`}),
            },
            data: {
                initialReportName: initialReportName
            }
        });
    }
});