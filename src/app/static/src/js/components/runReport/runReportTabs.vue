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
                                    <a id="run-link" class="nav-link active" data-toggle="tab" role="tab" @click="switchTab('runReport')">Run a report</a>
                                </li>
                                <li class="nav-item">
                                    <a id="logs-link" class="nav-link" data-toggle="tab" role="tab" @click="switchTab('reportLogs')">Report logs</a>
                                </li>
                            </ul>
                        </div>
                    </nav>
                </div>
            </div>
            <div class="col-12 col-md-8 tab-content">
                <div v-if="selectedTab === 'runReport'" class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="run-tab">
                    <div id="runReportVueApp">
                        <run-report :metadata="metadata" :initial-git-branches="initialGitBranches"></run-report>
                    </div>
                </div>
                <div v-if="selectedTab === 'reportLogs'" class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="log-tab">
                    <div>
                        <h2>Report logs</h2>
                        <p>Report logs coming soon!</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import runReport from "./runReport.vue"
    export default Vue.extend({
        name: "runReportTabs",
        components: {
            runReport
        },
        props: [
            "metadata",
            "initialGitBranches",
        ],
        data() {
            return {
                selectedTab: "runReport",
                reportKey: ""
            }
        },
        methods: {
            switchTab(tab){
                this.selectedTab = tab
            }
        }
    })
</script>