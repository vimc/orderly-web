<template>
    <div class="mb-3">
        <div class="pr-2 d-inline custom-control custom-checkbox">
            <input :id="draft.id"
                   v-model="selected"
                   type="checkbox"
                   class="custom-control-input">
            <label class="custom-control-label"
                   :for="draft.id">
                <a :href="draft.url">{{ draft.id }}</a>
            </label>
        </div>
        <span class="text-muted pl-3">{{ draft.parameter_values }}</span>
        <div class="changelog-container" :class="{'open': showChangelog}">
            <a v-if="draft.changelog.length > 0" href="#" class="text-muted" @click="toggleChangelog">changelog</a>
            <div v-for="log in draft.changelog"
                 v-show="showChangelog"
                 :key="log.value"
                 class="changelog bg-light">
                <div class="badge changelog-label" :class="'badge-' + log.css_class">
                    {{ log.label }}
                </div>
                <div class="changelog-item" :class="log.css_class">
                    {{ log.value }}
                </div>
            </div>
        </div>
    </div>
</template>
<script>
    export default {
        name: 'ReportDraft',
    props: ['draft', 'selectedIds', 'expandClicked', 'collapseClicked'],
        data() {
            return {
                showChangelog: false
            }
        },
        computed: {
        selected: {
            get() {
                return this.selectedIds[this.draft.id]
            },
            set(value) {
                this.$emit("select-draft", {id: this.draft.id, value})
            }
        }
        },
        watch: {
            expandClicked() {
                this.showChangelog = true;
            },
            collapseClicked() {
                this.showChangelog = false;
            }
        },
        methods: {
            toggleChangelog(e) {
                e.preventDefault();
                this.showChangelog = !this.showChangelog;
            }
        }
    }
</script>
