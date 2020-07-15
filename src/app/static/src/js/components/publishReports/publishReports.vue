<template>
    <div>
        <h2 class="h4 mb-4">Latest drafts</h2>
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

    export default {
        name: "publishReports",
        components: {DateGroup},
        props: ["reportsWithDrafts"]
    }
</script>
