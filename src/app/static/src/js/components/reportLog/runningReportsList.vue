<template>
    <vue-typeahead-bootstrap
        :data="sortedReports"
        :serializer="e => e.name"
        v-model="query"
        showOnFocus
        maxMatches="50"
        placeholder="Choose a report"
        @hit="$emit('update:key', $event.key)"
    >
        <template slot="append">
            <button class="btn btn-outline-secondary" v-on:click.prevent="clear" v-if="query">
                <x-icon/>
            </button>
        </template>
        <template slot="suggestion" slot-scope="{ data, htmlText }">
            <div>
                <span v-html="htmlText"></span>
                <span class="text-muted pl-3">Run started: {{
                        data.date ? new Intl.DateTimeFormat(undefined, {
                            weekday: "short",
                            month: "short",
                            day: "numeric",
                            year: "numeric",
                            hour: "numeric",
                            minute: "numeric"
                        }).format(new Date(data.date)) : 'never'
                    }}</span>
            </div>
        </template>
    </vue-typeahead-bootstrap>
</template>

<script lang="ts">
    import Vue from "vue";
    import VueTypeaheadBootstrap from "vue-typeahead-bootstrap"
    import XIcon from "../runReport/xIcon.vue"

    export default Vue.extend({
        name: "reportList",
        props: {
            "reports": Array,
            "report": String
        },
        components: {
            VueTypeaheadBootstrap,
            XIcon
        },
        methods: {
            clear() {
                this.query = "";
                // this.$emit("update:report", "");
                this.$emit('update:key', "");
            }
        },
        data() {
            return {
                query: ""
            }
        },
        computed: {
            sortedReports() {
                return this.reports.sort((a, b) => a.date.localeCompare(b.date)).reverse();
            }
        },
        beforeDestroy() {
            // this.$emit('update:report', "");
            this.$emit('update:key', "");
        }
    })

</script>
