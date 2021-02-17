<template>
    <div>
        <running-reports-details :reportKey="reportKey"></running-reports-details>
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
        data(){
            return {
                reportKey: "something"
            }
        }
    })
</script>
