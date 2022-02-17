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
    import {SELECTED_RUNNING_REPORT_TAB} from "../../utils/session";

    export default {
        name: 'RunReportInline',
        props: ['report'],
        computed: {
            runReportHref: function () {
                const url = `/run-report?report-name=${this.report.name}`
                return buildFullUrl(url)
            }
        },
        methods: {
            clearRunReportSession() {
                session.setSelectedTab(SELECTED_RUNNING_REPORT_TAB, "runReport");
            }
        }
    }
</script>