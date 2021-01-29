<template>
    <vue-typeahead-bootstrap
        :data="sortedReports"
        :serializer="e => e.name"
        v-model="query"
        showOnFocus
        placeholder="Choose a report"
        @hit="$emit('update:report', $event.name)"
    >
        <template slot="append">
            <button class="btn btn-outline-secondary" v-on:click.prevent="clear" v-if="query">
                🗙
            </button>
        </template>
        <template slot="suggestion" slot-scope="{ data, htmlText }">
            <div>
                <span v-html="htmlText"></span>
                <span class="text-muted pl-3">Last run: {{ data.date ? new Intl.DateTimeFormat(undefined, {weekday: "short", month: "short", day: "numeric", year: "numeric", hour: "numeric", minute: "numeric"}).format(new Date(data.date)) : 'never' }}</span>
            </div>
        </template>
    </vue-typeahead-bootstrap>
</template>

<script>
    import VueTypeaheadBootstrap from "vue-typeahead-bootstrap";

    export default {
        name: "reportList",
        props: {
            "reports": Array,
            "report": String
        },
        components: {
            VueTypeaheadBootstrap
        },
        methods: {
            clear() {
                this.query = "";
                this.$emit("update:report", "");
            }
        },
        data() {
            return {
                query: ""
            }
        },
        computed: {
            sortedReports() {
                return this.reports.sort((a, b) => a.name.localeCompare(b.name));
            }
        },
        beforeDestroy() {
            this.$emit('update:report', "")
        }
    }

</script>
