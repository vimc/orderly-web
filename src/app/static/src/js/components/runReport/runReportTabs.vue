<template>
    <div id="report-log-container" class="mt-3">
        <div class="row">
            <div class="col-12 col-md-4 col-xl-3">
                <div class="sidebar pb-4 pb-md-0">
                    <nav class="pl-0 pr-0 pr-md-4 navbar navbar-light">
                        <button type="button" class="d-md-none navbar-toggler" data-toggle="collapse"
                                data-target="#sidebar">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                        <div id="sidebar" class="d-md-block mt-4 mt-md-0 collapse navbar-collapse">
                            <ul class="nav flex-column list-unstyled mb-0">
                                <li class="nav-item">
                                    <a id="run-link"
                                       class="nav-link"
                                       :class="{active: selectedTab == 'runReport'}"
                                       data-toggle="tab"
                                       role="tab"
                                       href="#"
                                       @click="switchTab('runReport')">Run a report</a>
                                </li>
                                <li class="nav-item">
                                    <a id="logs-link"
                                       class="nav-link"
                                       :class="{active: selectedTab == 'reportLogs'}"
                                       data-toggle="tab"
                                       role="tab"
                                       href="#"
                                       @click="switchTab('reportLogs')">Report logs</a>
                                </li>
                            </ul>
                        </div>
                    </nav>
                </div>
            </div>
            <div class="col-12 col-md-8 tab-content">
                <div v-if="selectedTab === 'runReport'" id="run-tab" class="tab-pane active pt-4 pt-md-1"
                     role="tabpanel">
                    <div id="runReportVueApp">
                        <run-report :initial-report-name="initialReportName"
                                    @changeTab="switchTab('reportLogs')"
                                    @update:key="setSelectedReportKey"></run-report>
                    </div>
                </div>
                <div v-if="selectedTab === 'reportLogs'" id="logs-tab" class="tab-pane active pt-4 pt-md-1"
                     role="tabpanel">
                    <div>
                        <report-log :selected-running-report-key="selectedRunningReportKey"
                                    @update:key="setSelectedReportKey"></report-log>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import runReport from "./runReport.vue"
    import reportLog from "./../reportLog/reportLog.vue"
    import {SELECTED_RUNNING_REPORT_KEY, SELECTED_RUNNING_REPORT_TAB, session} from "./../../utils/session.js"

    export default Vue.extend({
        name: "RunReportTabs",
        components: {
            reportLog,
            runReport
        },
        props: [
            "initialReportName"
        ],
        data() {
            return {
                selectedTab: session.getSelectedTab(SELECTED_RUNNING_REPORT_TAB) || "runReport",
                selectedRunningReportKey: session.getSelectedKey(SELECTED_RUNNING_REPORT_KEY)
            }
        },
        methods: {
            setSelectedReportKey(e) {
                this.selectedRunningReportKey = e;
                session.setSelectedKey(SELECTED_RUNNING_REPORT_KEY, e);
            },
            switchTab(tab) {
                this.selectedTab = tab;
                session.setSelectedTab(SELECTED_RUNNING_REPORT_TAB, tab);
            }
        }
    })
</script>
