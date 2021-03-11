<template>
    <div id="report-log-container" class="mt-3">
        <!-- <div id="report-key">
            <div class="col-auto">
                <h2>Test header2</h2> -->
            <!-- <running-reports-details id="report-details" :reportKey="reportKey"></running-reports-details> -->
            <!-- <run-report :metadata="metadata" :initial-git-branches="initialGitBranches"></run-report> -->
            <!-- </div>
        </div> -->
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
                                <a id="run-link" class="nav-link active" data-toggle="tab" href="#run-tab" role="tab">Run a report</a>
                            </li>
                            <li class="nav-item">
                                <a id="logs-link" class="nav-link" data-toggle="tab" href="#logs-tab" role="tab">Report logs</a>
                            </li>
                        </ul>
                    </div>
                </nav>
            </div>
        </div>
        <div class="col-12 col-md-8 tab-content">
            <div class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="run-tab">
                <h2>Test header</h2>
                <div id="runReportVueApp">
                    <run-report :metadata="metadata" :initial-git-branches="initialGitBranches"></run-report>
                    <!-- <run-report-tabs :metadata="runReportMetadata" :initial-git-branches="gitBranches"></run-report-tabs> -->
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    // import runningReportsDetails from "./runningReportsDetails.vue";
    import runReport from "./runReport.vue"
    export default Vue.extend({
        name: "runReportTabs",
        components: {
            // runningReportsDetails
            runReport
        },
        props: [
            "metadata",
            "initialGitBranches",
        ],
        mounted() {
            console.log('run report tabs mounted', this.metadata, this.initialGitBranches)
        },
        data() {
            return {
                reportKey: ""
            }
        }
    })
</script>