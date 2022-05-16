<template>
    <div>
        <h2>Run a report</h2>
        <form class="mt-3">
            <git-selections/>
            <div v-if="showReports" id="report-form-group" class="form-group row">
                <label for="report" class="col-sm-2 col-form-label text-right">Report</label>
                <div class="col-sm-6">
                    <report-list id="report"/>
                </div>
            </div>
        </form>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import GitSelections from "./gitSelections.vue"
    import ReportList from "./reportList.vue"
    import {ReportsState} from "../../../store/reports/reports";
    import {mapStateProp} from "../../utils";

    interface Computed {
        showReports: number
    }

    export default Vue.extend<unknown, unknown, Computed, unknown>({
        name: "RunReport",
        components: {
            GitSelections,
            ReportList
        },
        computed: {
            showReports: mapStateProp<ReportsState, number>("reports", (state: ReportsState) => {
                return state.reports.length
            })
        }
    })
</script>
