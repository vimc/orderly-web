<template>
    <div>
        <v-select :options="reports"
                  :value="selectedReport"
                  label="name"
                  placeholder="Choose a report"
                  @input="selectReport">
            <template #option="{ name, date }">
                <div>
                    <span>{{ name }}</span>
                    <span class="text-muted pl-3">Last run: {{ getFormattedDate(date) }}</span>
                </div>
            </template>
        </v-select>
        <error-alert v-if="reportsError" :error="reportsError"></error-alert>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import vSelect from "vue-select";
    import {formatDate} from "../../../utils/helpers";
    import {ReportWithDate, RunnerRootState, Error} from "../../../utils/types";
    import {mapState} from "vuex";
    import {mapMutationByName} from "../../utils";
    import {ReportsMutation} from "../../../store/reports/mutations";
    import ErrorAlert from "../../ErrorAlert.vue";

    interface Computed {
        selectedReport: ReportWithDate,
        reports: ReportWithDate[],
        reportsError: Error
    }

    interface Methods {
        getFormattedDate: (date: Date) => string
        selectReport: (value: ReportWithDate) => void
    }

    export default Vue.extend<unknown, Methods, Computed, unknown>({
        name: "ReportList",
        components: {
            vSelect,
            ErrorAlert
        },
        computed: {
            ...mapState({
                selectedReport: (state: RunnerRootState) => state.reports.selectedReport,
                reports: (state: RunnerRootState) => state.reports.reports,
                reportsError: (state: RunnerRootState) => state.reports.reportsError
            })
        },
        methods: {
            getFormattedDate(date) {
                return date
                    ? formatDate(date)
                    : 'never';
            },
            selectReport: mapMutationByName("reports", ReportsMutation.SelectReport)
        }
    });
</script>