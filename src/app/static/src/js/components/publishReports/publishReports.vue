<template>
    <div>
        <h2 class="h4 mb-4">Latest drafts</h2>
        <report v-for="report in reportsWithDrafts"
                :report="report"
                :selected-ids="selectedIds"
                @change="change"></report>
    </div>
</template>
<script>
    import report from "./report";

    export default {
        name: "publishReports",
        components: {report},
        props: ["reportsWithDrafts"],
        data() {
            const selectedIds = {}
            this.reportsWithDrafts.map(r => r.date_groups
                .map(g => g.drafts.map(d => selectedIds[d.id] = false)))
            return {
                selectedIds
            }
        },
        methods: {
            change(value) {
                if (value.id) {
                    this.selectedIds[value.id] = value.value
                }
                if (value.ids) {
                    value.ids.map(id => this.selectedIds[id] = value.value)
                }
            }
        }
    }
</script>
