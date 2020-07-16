<template>
    <div>
        <div class="pr-2 d-inline custom-control custom-checkbox">
            <input type="checkbox"
                   class="custom-control-input"
                   v-model="selected"
                   :id="date">
            <label class="custom-control-label h6"
                   :for="date">
                {{date}}
            </label>
        </div>
        <div class="ml-4">
            <report-draft v-for="draft in drafts"
                          :draft="draft"
                          :selected-ids="selectedIds"
                          @change="handleChangeFromChild"></report-draft>
        </div>
    </div>
</template>

<script>
    import ReportDraft from "./reportDraft";
    import selectableParentMixin from "./selectableParentMixin";

    export default {
        name: "dateGroup",
        components: {ReportDraft},
        props: ["date", "drafts", "selectedIds", "selectedDates"],
        computed: {
            selected: {
                get() {
                    return this.selectedDates[this.date]
                },
                set(value) {
                    this.$emit("change", {ids: this.draftIds, value});
                    this.$emit("change-group", {date: this.date, value});
                }
            },
            draftIds() {
                return this.drafts.map(d => d.id);
            }
        },
        methods: {
            selectGroup(e) {
                const value = e.target.checked
                this.selected = value
                this.$emit("change", {ids: this.draftIds, value});
                this.$emit("change-group", {date: this.date, value});
            },
            handleChangeFromChild(value) {
                if (!value.value) {
                    // don't want the parent checkbox selected if any child is not
                    this.$emit("change-group", {date: this.date, false});
                }
                this.$emit("change", value)
            }
        }
    }
</script>
