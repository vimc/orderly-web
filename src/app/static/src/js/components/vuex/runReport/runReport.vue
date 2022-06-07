<template>
    <div>
        <h2>Run a report</h2>
        <form class="mt-3">
            <git-selections/>
            <report-list/>
        </form>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import GitSelections from "./gitSelections.vue"
    import ReportList from "./reportList.vue"

    export default Vue.extend<unknown, unknown, unknown, unknown>({
        name: "RunReport",
        components: {
            GitSelections,
            ReportList
        }
    })
</script>
