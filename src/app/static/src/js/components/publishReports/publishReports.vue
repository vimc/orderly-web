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
        <!-- use first draft id as the key as just need something unique -->
        <report v-for="report in reportsWithDrafts"
                :key="report.date_groups[0].drafts[0].id"
                v-if="!publishedOnly || report.previously_published"
                :report="report"
                :selected-ids="selectedIds"
                :selected-dates="selectedDates"
                :expand-clicked="expandClicked"
                :collapse-clicked="collapseClicked"
                @select-draft="handleDraftSelect"
                @select-group="handleGroupSelect"></report>
        <button class="btn btn-published"
                @click="publishDrafts">Publish
        </button>
        <error-info :api-error="apiError" :default-message="defaultMessage"></error-info>
    </div>
</template>
<script>
import Report from "./report";
import {api} from "../../utils/api";
import ErrorInfo from "../errorInfo";

export default {
    name: "publishReports",
    components: {ErrorInfo, Report},
    data() {
        return {
            selectedDates: {},
            selectedIds: {},
            reportsWithDrafts: [],
            publishedOnly: false,
            expandClicked: 0,
            collapseClicked: 0,
            apiError: null,
            defaultMessage: "Something went wrong. Please try again or contact support."
        }
    },
    methods: {
        publishDrafts() {
            this.apiError = null;
            const ids = Object.keys(this.selectedIds)
                .filter(id => this.selectedIds[id])
            api.post("/bulk-publish/", {ids})
                .then(() => {
                    this.getReportsWithDrafts();
                })
                .catch((error) => {
                    this.apiError = error;
                })
        },
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
        getReportsWithDrafts() {
            api.get("/report-drafts/")
                .then(({data}) => {
                    const selectedIds = {}
                    const selectedDates = {}
                    data.data.map(r => r.date_groups
                        .map(g => {
                            selectedDates[g.date] = false;
                            g.drafts.map(d => selectedIds[d.id] = false)
                        }));
                    this.selectedDates = selectedDates
                    this.selectedIds = selectedIds
                    this.reportsWithDrafts = data.data
                })
        },
        expandChangelogs(e) {
            e.preventDefault();
            this.expandClicked = this.expandClicked + 1;
        },
        collapseChangelogs(e) {
            e.preventDefault();
            this.collapseClicked = this.collapseClicked + 1;
        }
    },
    watch: {
        publishedOnly(newVal) {
            if (newVal) {
                this.reportsWithDrafts.map(r => {
                    if (!r.previously_published) {
                        // if this report has not been published, deselect all its dates and drafts
                        r.date_groups
                            .map(g => {
                                this.selectedDates[g.date] = false;
                                g.drafts.map(d => this.selectedIds[d.id] = false)
                            })
                    }
                });
            }
        }
    },
    mounted() {
        this.getReportsWithDrafts();
    }
}
</script>
