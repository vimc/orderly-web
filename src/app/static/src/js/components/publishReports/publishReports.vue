<template>
    <div>
        <h1 class="h3">Publish reports</h1>
        <span class="text-muted">
            Here you can publish the latest drafts (unpublished versions) of reports in bulk.
            You can also manage the publish status of an individual report version directly from its report page.
        </span>
        <div class="mb-4 mt-2">
            <div class="mb-2 custom-control custom-checkbox">
                <input type="checkbox" class="custom-control-input" id="publishedOnly" v-model="publishedOnly">
                <label class="custom-control-label" for="publishedOnly">
                    Only show reports with previously published versions
                </label>
            </div>
            <a href="#" @click="expandChangelogs">
                Expand all changelogs
            </a>
            <span>&nbsp;/&nbsp;</span>
            <a href="#" @click="collapseChangelogs">
                Collapse all changelogs
            </a>
        </div>
        <div v-for="report in reportsWithDrafts" v-if="!publishedOnly || report.previously_published" class="report">
            <h5>{{report.display_name}}</h5>
            <div class="ml-5">
                <date-group v-for="group in report.date_groups"
                            :date="group.date"
                            :drafts="group.drafts"
                            :expand-clicked="expandClicked"
                            :collapse-clicked="collapseClicked"></date-group>
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
                publishedOnly: false,
                expandClicked: 0,
                collapseClicked: 0
            }
        },
        methods: {
            expandChangelogs(e) {
                e.preventDefault();
                this.expandClicked = this.expandClicked + 1;
            },
            collapseChangelogs(e) {
                e.preventDefault();
                this.collapseClicked = this.collapseClicked + 1;
            }
        }
    }
</script>
