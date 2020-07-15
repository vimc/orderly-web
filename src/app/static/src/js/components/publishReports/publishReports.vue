<template>
    <div>
        <div class="mb-4">
            <h2 class="h4 mb-4">Latest drafts</h2>
            <a href="#" @click="expandChangelogs">
                Expand all changelogs
            </a>
            <span>&nbsp;/&nbsp;</span>
            <a href="#" @click="collapseChangelogs">
                Collapse all changelogs
            </a>
        </div>
        <div v-for="report in reportsWithDrafts" class="report">
            <h5>{{report.display_name}}</h5>
            <div class="ml-5">
                <date-group v-for="group in report.date_groups"
                            :date="group.date"
                            :drafts="group.drafts"
                            :expand="expand"
                            :collapse="collapse"></date-group>
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
                expand: 0,
                collapse: 0
            }
        },
        methods: {
            expandChangelogs(e) {
                e.preventDefault();
                this.expand = this.expand + 1;
            },
            collapseChangelogs(e) {
                e.preventDefault();
                this.collapse = this.collapse + 1;
            }
        }
    }
</script>
