<template>
    <div id="report-log-container" class="mt-3">
        <div class="row">
            <div class="col-12 col-md-4 col-xl-3">
                <div class="sidebar pb-4 pb-md-0">
                    <nav class="pl-0 pr-0 pr-md-4 navbar navbar-light">
                        <button type="button" class="d-md-none navbar-toggler"
                                data-toggle="collapse"
                                data-target="#sidebar">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                        <div id="sidebar" class="d-md-block mt-4 mt-md-0 collapse navbar-collapse">
                            <ul class="nav flex-column list-unstyled mb-0">
                                <li class="nav-item">
                                    <a id="run-link"
                                       class="nav-link"
                                       :class="{active: selectedTab === 'RunReport'}"
                                       role="tab"
                                       href="#"
                                       @click="switchTab('RunReport')">Run a report</a>
                                </li>
                                <li class="nav-item">
                                    <a id="logs-link"
                                       class="nav-link"
                                       :class="{active: selectedTab === 'ReportLogs'}"
                                       role="tab"
                                       href="#"
                                       @click="switchTab('ReportLogs')">Report logs</a>
                                </li>
                            </ul>
                        </div>
                    </nav>
                </div>
            </div>
            <div class="col-12 col-md-8 tab-content">
                <div v-if="selectedTab === 'RunReport'"
                     id="run-tab" class="tab-pane active pt-4 pt-md-1"
                     role="tabpanel">
                    <run-report/>
                </div>
                <div v-if="selectedTab === 'ReportLogs'"
                     id="logs-tab" class="tab-pane active pt-4 pt-md-1"
                     role="tabpanel">
                    <report-log/>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import runReport from "./runReport.vue"
    import reportLog from "./../reportLog/reportLog.vue"
    import {mapMutations, mapState} from "vuex";
    import {RunReportRootState, RunReportTabName} from "../../../store/runReport/store";
    import {RunReportMutation} from "../../../store/runReport/mutations";

    interface Computed {
        selectedTab: string
    }

    interface Methods {
        switchTab: (tabName: RunReportTabName) => void
    }

    export default Vue.extend<Record<string, never>, Methods, Computed, Record<string, never>>({
        name: "RunReportTabs",
        components: {
            reportLog,
            runReport
        },
        computed: {
            ...mapState({
                selectedTab: (state: RunReportRootState) => state.selectedTab
            })
        },
        methods: {
            ...mapMutations({switchTab: `${RunReportMutation.SwitchTab}`})
        }
    })
</script>
