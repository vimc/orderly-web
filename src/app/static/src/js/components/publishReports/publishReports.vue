<template>
    <div>
        <h2>Latest drafts</h2>
        <div v-for="report in reportsWithDrafts" class="report">
            <span>{{report.display_name}}</span>
            <div class="ml-3">
                <date-group v-for="group in report.date_groups"
                            :date="group.date"
                            :drafts="group.drafts"></date-group>
            </div>
        </div>
    </div>
</template>
<script>
    import DateGroup from "./dateGroup";

    export default {
        name: "publishReports",
        components: {DateGroup},
        props: ["reportsWithDrafts"]
    }
</script>
