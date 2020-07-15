<template>
    <div class="mb-3">
        <a :href="draft.url">{{draft.id}}</a>
        <span class="text-muted pl-3">{{draft.parameter_values}}</span>
        <div class="changelog-container" :class="{'open': showChangelog}">
            <a v-if="draft.changelog.length > 0" href="#" class="text-muted" @click="toggleChangelog">changelog</a>
            <div v-show="showChangelog" v-for="log in draft.changelog" class="changelog bg-light">
                <div class="badge changelog-label" :class="'badge-' + log.css_class">{{log.label}}</div>
                <div class="changelog-item" :class="log.css_class">
                    {{log.value}}
                </div>
            </div>
        </div>
    </div>
</template>
<script>
    export default {
        name: 'reportDraft',
        props: ['draft'],
        data() {
            return {
                showChangelog: false
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