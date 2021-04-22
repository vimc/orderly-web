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
                        <div class="d-md-block mt-4 mt-md-0 collapse navbar-collapse" id="sidebar">
                            <ul class="nav flex-column list-unstyled mb-0">
                                <li class="nav-item">
                                    <a id="run-link" class="nav-link" :class="{active: selectedTab == 'runReport'}" data-toggle="tab" role="tab" href="#" @click="switchTab('runReport')">Run a report</a>
                                </li>
                                <li class="nav-item">
                                    <a id="logs-link" class="nav-link" :class="{active: selectedTab == 'reportLogs'}" data-toggle="tab" role="tab" href="#" @click="switchTab('reportLogs')">Report logs</a>
                                </li>
                            </ul>
                        </div>
                    </nav>
                </div>
            </div>
            <div class="col-12 col-md-8 tab-content">
                <div v-if="selectedTab === 'runReport'" class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="run-tab">
                    <div id="runReportVueApp">
                        <run-report :metadata="metadata"
                                    :initial-git-branches="initialGitBranches"
                                    :initial-report-name="initialReportName"
                                    @changeTab="switchTab('reportLogs')"
                                    @update:key="setSelectedReportKey"></run-report>
                    </div>
                </div>
                <div v-if="selectedTab === 'reportLogs'" class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="logs-tab">
                    <div>
                        <report-log @update:key="setSelectedReportKey" :selectedRunningReportKey="selectedRunningReportKey"></report-log>
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
    export default Vue.extend({
        name: "runReportTabs",
        components: {
            reportLog,
            runReport
        },
        props: [
            "metadata",
            "initialGitBranches",
            "initialReportName"
        ],
        data() {
            return {
                selectedTab: "runReport",
                selectedRunningReportKey: ""
            }
        },
        watch: {
            selectedRunningReportKey(){
                console.log(this.selectedRunningReportKey)
            }
        },
        methods: {
            setSelectedReportKey(e){
                this.selectedRunningReportKey = e
            },
            switchTab(tab){
                this.selectedTab = tab
            }
        }
    })
</script>
