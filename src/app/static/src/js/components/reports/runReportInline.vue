<template>
    <div id="run-report" class="mt-5">
        <label class="font-weight-bold">Run</label>
        <div>
            <div>Run this report to create a new version.</div>
        </div>
        <a id="run-report-link" :href="runReportHref" @click="clearRunReportSession">Run report</a>
    </div>
</template>

<script>
    import {buildFullUrl} from "../../utils/api";
    import {session} from "./../../utils/session.js"

    export default {
        name: 'runReport',
        props: ['report'],
        computed: {
            runReportHref: function () {
                const url = `/run-report?report-name=${this.report.name}`
                return buildFullUrl(url)
            }
        },
        methods: {
            clearRunReportSession(){
                session.setSelectedRunningReportKey("");
                session.setSelectedTab("runReport");
            }
        }
    }
</script>