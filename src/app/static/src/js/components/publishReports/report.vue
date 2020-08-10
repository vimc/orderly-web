<template>
    <div class="report">
        <div class="pr-2 d-inline custom-control custom-checkbox">
            <input type="checkbox"
                   class="custom-control-input"
                   :checked="selected"
                   @change="selectReport"
                   :id="report.display_name">
            <label class="custom-control-label h5"
                   :for="report.display_name">
                {{ report.display_name }}
            </label>
        </div>
        <div class="ml-4 mt-2">
            <date-group v-for="group in report.date_groups"
                        :date="group.date"
                        :drafts="group.drafts"
                        :selected-ids="selectedIds"
                        :selected-dates="selectedDates"
                        @select-draft="handleDraftSelect"
                        @select-group="handleGroupSelect"
                        :expand-clicked="expandClicked"
                        :collapse-clicked="collapseClicked"></date-group>
        </div>
    </div>
</template>
<script>
import DateGroup from "./dateGroup";

export default {
    components: {DateGroup},
    props: ["report", "selectedIds", "selectedDates", "expandClicked", "collapseClicked"],
    data() {
        return {
            selected: false
        }
    },
    computed: {
        dates() {
            return this.report.date_groups.map(g => g.date)
        },
        draftIds() {
            return [].concat.apply([], this.report.date_groups.map(g => g.drafts.map(d => d.id)))
        }
    },
    methods: {
        selectReport(e) {
            const value = e.target.checked
            this.selected = value
            this.$emit("select-draft", {ids: this.draftIds, value});
            this.$emit("select-group", {dates: this.dates, value});
        },
        handleDraftSelect(value) {
            if (!value.value) {
                console.log("hi")
                // don't want the parent checkbox selected if any child is not
                this.selected = false
            }
            this.$emit("select-draft", value)
        },
        handleGroupSelect(value) {
            this.$emit("select-group", value);
        }
    }
}
</script>