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
                <span class="listOption" v-html="htmlText"></span>
                <span class="text-muted pl-3">Run started: {{ 
                    new Intl.DateTimeFormat(undefined, {
                            weekday: "short",
                            month: "short",
                            day: "numeric",
                            year: "numeric",
                            hour: "numeric",
                            minute: "numeric"
                        }).format(new Date(data.date)) 
                }}</span>
            </div>
        </template>
    </vue-typeahead-bootstrap>
</template>

<script lang="ts">
    import Vue from "vue";
    import VueTypeaheadBootstrap from "vue-typeahead-bootstrap"
    import XIcon from "../runReport/xIcon.vue"
    import runningReportDetails from "./runningReportDetails.vue";

    export default Vue.extend({
        name: "runningReportsList",
        props: {
            "reports": Array,
            "initialSelectedKey": String
        },
        components: {
            VueTypeaheadBootstrap,
            XIcon,
            runningReportDetails
        },
        methods: {
            clear() {
                this.query = "";
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
            this.$emit('update:key', "");
        }
    })

</script>
