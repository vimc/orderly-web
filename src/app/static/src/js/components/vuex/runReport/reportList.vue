<template>
    <v-select :options="reports"
              :value="selectedReport"
              label="name"
              placeholder="Choose a report"
              @input="selectReport">
        <template #option="{ name, date }">
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
import {longTimestamp} from "../../../utils/helpers";
import {ReportWithDate, RunnerRootState} from "../../../utils/types";
import {mapState} from "vuex";
import {mapMutationByName} from "../../utils";
import {ReportsMutation} from "../../../store/reports/mutations";

interface Computed {
    selectedReport: ReportWithDate,
    reports: ReportWithDate[]
}

interface Methods {
    formatDate: (date: Date) => string
    selectReport: (value: ReportWithDate) => void
}

export default Vue.extend<unknown, Methods, Computed, unknown>({
    name: "ReportList",
    components: {
        vSelect
    },
    computed: {
        ...mapState({
            selectedReport: (state: RunnerRootState) => state.reports.selectedReport,
            reports: (state: RunnerRootState) => state.reports.reports
        })
    },
    methods: {
        formatDate(date) {
            return date
                ? longTimestamp(new Date(date))
                : 'never';
        },
        selectReport: mapMutationByName("reports", ReportsMutation.SelectReport)
    }
});
</script>