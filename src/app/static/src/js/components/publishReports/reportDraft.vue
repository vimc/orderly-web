<template>
    <div>
        <a :href="draft.url">{{draft.id}}</a>
        <span class="text-muted pl-3">{{draft.parameter_values}}</span>
        <div :class="{'open': showChangelog}">
            <div v-show="showChangelog" v-for="log in draft.changelog" class="changelog" >
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
        }
    }
</script>