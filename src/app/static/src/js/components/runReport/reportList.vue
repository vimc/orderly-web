<template>
    <v-select :options="sortedReports" :value="selectedReport" label="name" placeholder="Choose a report"
              @input="setSelected">
        <template id="optionTemplate" #option="{ name, date }">
            <div>
                <span>{{ name }}</span>
                <span class="text-muted pl-3">Last run: {{ formatDate(date) }}</span>
            </div>
        </template>
    </v-select>
</template>

<script lang="ts">
import Vue from "vue";
import vSelect from "vue-select";
import {longTimestamp} from "../../utils/helpers.ts";
import {ReportWithDate} from "../../utils/types";

interface Props {
    reports: ReportWithDate[]
    selectedReport: ReportWithDate
}

interface Computed {
    sortedReports: ReportWithDate[]
}

interface Methods {
    formatDate: (date: Date) => string
    setSelected: (value: ReportWithDate) => void
}

export default Vue.extend<void, Methods, Computed, Props>({
        name: "reportList",
        components: {
            vSelect
        },
        props: {
            "reports": Array,
            "selectedReport": Object
        },
        computed: {
            sortedReports() {
                return [...this.reports].sort((a, b) => a.name.localeCompare(b.name));
            }
        },
        methods: {
            formatDate(date) {
                return date ? longTimestamp(new Date(date)) : 'never';
            },
            setSelected(value) {
                this.$emit("update:selectedReport", value);
            }
        }
    }
);

</script>
