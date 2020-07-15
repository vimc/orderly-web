<template>
    <div>
        <div class="mb-4">
            <label class="font-weight-bold h5 mb-4 d-inline">Latest drafts</label>
            <div class="h5 ml-4 d-inline custom-control custom-checkbox">
                <input type="checkbox" class="custom-control-input" id="publishedOnly" v-model="publishedOnly">
                <label class="custom-control-label" for="publishedOnly">
                    Only show reports with previously published versions
                </label>
            </div>
        </div>
        <div v-for="report in reportsWithDrafts" v-if="!publishedOnly || report.previously_published" class="report">
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
        props: ["reportsWithDrafts"],
        data() {
            return {
                publishedOnly: false
            }
        }
    }
</script>
