<template>
    <div>
        <h1 class="h3">Publish reports</h1>
        <span class="text-muted">
            Here you can publish the latest drafts (unpublished versions) of reports in bulk.
            You can also manage the publish status of an individual report version directly from its report page.
        </span>
        <div v-for="report in reportsWithDrafts" class="report">
            <h5>{{report.display_name}}</h5>
            <div class="ml-5">
                <date-group v-for="group in report.date_groups"
                            :date="group.date"
                            :drafts="group.drafts"></date-group>
            </div>
        </div>
    </div>
</template>
<script>
    import DateGroup from "./dateGroup";
    import {api} from "../../utils/api";

    export default {
        name: "publishReports",
        components: {DateGroup},
        data() {
            return {reportsWithDrafts: []}
        },
        methods: {
            getReportsWithDrafts() {
                api.get("/report-drafts/")
                    .then(({data}) => {
                        this.reportsWithDrafts = data.data
                    })
            }
        },
        mounted() {
            this.getReportsWithDrafts();
        }
    }
</script>
