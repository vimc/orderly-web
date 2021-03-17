<template>
    <div id="run-report" class="mt-5">
        <label class="font-weight-bold">Run</label>
        <div>
            <div>Run this report to create a new version.</div>
        </div>
        <a id="run-report-link" :href="runReportHref">Run report</a>
    </div>
</template>

<script>
    export default {
        name: 'runReport',
        props: ['report'],
        computed: {
            runReportHref: function () {
                const url = `/run-report?report-name=${this.report.name}`
                return buildFullUrl(url)
            }
        }
    }
</script>