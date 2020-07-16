<template>
    <div>
        <h2 class="h4 mb-4">Latest drafts</h2>
        <report v-for="report in reportsWithDrafts"
                :report="report"
                :selected-ids="selectedIds"
                :selected-dates="selectedDates"
                @select-draft="handleDraftSelect"
                @select-group="handleGroupSelect"></report>
        <button class="btn btn-published"
                @click="publishDrafts">Publish
        </button>
    </div>
</template>
<script>
    import report from "./report";
    import {api} from "../../utils/api";

    export default {
        name: "publishReports",
        components: {report},
        props: ["reportsWithDrafts"],
        data() {
            const selectedIds = {}
            const selectedDates = {}
            this.reportsWithDrafts.map(r => r.date_groups
                .map(g => {
                    selectedDates[g.date] = false;
                    g.drafts.map(d => selectedIds[d.id] = false)
                }))
            return {
                selectedDates,
                selectedIds
            }
        },
        methods: {
            handleDraftSelect(value) {
                if (value.id) {
                    this.selectedIds[value.id] = value.value
                }
                if (value.ids) {
                    value.ids.map(id => this.selectedIds[id] = value.value)
                }
            },
            handleGroupSelect(value) {
                if (value.date) {
                    this.selectedDates[value.date] = value.value
                }
                if (value.dates) {
                    value.dates.map(d => this.selectedDates[d] = value.value)
                }
            },
            publishDrafts() {
                const ids = Object.keys(this.selectedIds)
                    .filter(id => this.selectedIds[id])
                api.post("/bulk-publish/", {ids})
                    .then(() => {
                        // refresh reports
                    })
                    .catch((error) => {
                        console.log(error)
                    })
            }
        }
    }
</script>
