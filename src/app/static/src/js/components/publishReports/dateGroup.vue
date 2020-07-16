<template>
    <div>
        <div class="pr-2 d-inline custom-control custom-checkbox">
            <input type="checkbox"
                   class="custom-control-input"
                   :value="selected"
                   @change="selectGroup"
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
                          @change="change"></report-draft>
        </div>
    </div>
</template>

<script>
    import ReportDraft from "./reportDraft";

    export default {
        name: "dateGroup",
        components: {ReportDraft},
        props: ["date", "drafts", "selectedIds"],
        data() {
            return {
                selected: false
            }
        },
        computed: {
            draftIds() {
                return this.drafts.map(d => d.id);
            }
        },
        methods: {
            change(value) {
                if (!value.value) {
                    // don't want the parent checkbox selected if any child is not
                    this.selected = false
                }
                this.$emit("change", value);
            },
            selectGroup(e) {
                const value = e.target.checked
                this.selected = value
                this.$emit("change", {ids: this.draftIds, value});
            }
        }
    }
</script>
