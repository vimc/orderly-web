<template>
    <div>
        <div class="pr-2 d-inline custom-control custom-checkbox">
            <input :id="date"
                   v-model="selected"
                   type="checkbox"
                   class="custom-control-input">
            <label class="custom-control-label h6"
                   :for="date">
                {{ date }}
            </label>
        </div>
        <div class="ml-4">
            <report-draft v-for="draft in drafts"
                          :key="draft.id"
                          :draft="draft"
                          :selected-ids="selectedIds"
                          :expand-clicked="expandClicked"
                          :collapse-clicked="collapseClicked"
                          @select-draft="handleDraftSelect"></report-draft>
        </div>
    </div>
</template>

<script>
    import ReportDraft from "./reportDraft";

    export default {
        name: "DateGroup",
        components: {ReportDraft},
        props: ["date", "drafts", "selectedIds", "selectedDates", "expandClicked", "collapseClicked"],
        computed: {
            selected: {
                get() {
                    return this.selectedDates[this.date]
                },
                set(value) {
                    this.$emit("select-draft", {ids: this.draftIds, value});
                    this.$emit("select-group", {date: this.date, value});
                }
            },
            draftIds() {
                return this.drafts.map(d => d.id);
            }
        },
        methods: {
            handleDraftSelect(value) {
                if (!value.value) {
                    // don't want the parent checkbox selected if any child is not
                    this.$emit("select-group", {date: this.date, value: false});
                }
                this.$emit("select-draft", value)
            }
        }
    }
</script>
