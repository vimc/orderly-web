<template>
    <v-select v-model="selectedKey" :options="sortedReports" label="name" :reduce="(label) => label.key"
              :clearable="false" placeholder="Choose a report">
        <template #option="{ name, date }">
            {{ name }} <span class="text-muted pl-3">Run started: {{ formatDate(date) }}</span>
        </template>
    </v-select>
</template>

<script lang="ts">
    import Vue from "vue";
    import {longTimestamp} from "../../utils/helpers.ts";
    import vSelect from "vue-select";

    export default Vue.extend({
        name: "RunningReportsList",
        components: {
            vSelect
        },
        props: {
            "reports": Array,
            "initialSelectedKey": String
        },
        data() {
            return {
                selectedKey: this.initialSelectedKey
            };
        },
        computed: {
            sortedReports() {
                return [...this.reports].sort((a, b) => a.date.localeCompare(b.date)).reverse();
            }
        },
        watch: {
            selectedKey(val) {
                this.$emit('update:key', val);
            }
        },
        methods: {
            formatDate(date) {
                return longTimestamp(new Date(date));
            }
        }
    })
</script>
