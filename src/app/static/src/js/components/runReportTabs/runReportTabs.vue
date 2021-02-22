<template>
    <div id="report-log-container" class="mt-3">
        <div id="report-key" v-if="reportKey">
            <div class="col-auto">
            <running-reports-details id="report-details" :reportKey="reportKey"></running-reports-details>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import runningReportsDetails from "./runningReportsDetails.vue";

    export default Vue.extend({
        name: "runReportTabs",
        components: {
            runningReportsDetails
        },
        data() {
            return {
                reportKey: ""
            }
        }
    })
</script>
