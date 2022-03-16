<template>
    <div>
        <h2>Running report logs</h2>
        <div v-if="showReports">
            <div id="logs-form-group" class="form-group row">
                <label for="logs" class="col-sm-2 col-form-label text-right">Show logs for</label>
                <div class="col-sm-6">
                    <running-reports-list id="logs"
                                          :reports="reports"
                                          :initial-selected-key="selectedRunningReportKey"
                                          v-on="$listeners"/>
                </div>
            </div>
            <div v-if="selectedRunningReportKey" id="running-report-details" class="form-group row text-right">
                <label for="details" class="col-sm-2 col-form-label text-right">Log details</label>
                <div class="col-sm-7">
                    <running-report-details id="details"
                                            :report-key="selectedRunningReportKey"></running-report-details>
                </div>
            </div>
        </div>
        <div v-else>
            <p id="noReportsRun">
                No reports have been run yet
            </p>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import RunningReportsList from "./runningReportsList.vue"
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import runningReportDetails from "./runningReportDetails.vue";

    interface Computed {
        showReports: boolean
    }

    interface Methods {
        getAllReports: () => void
    }

    interface Data {
        reports: [],
        error: string,
        defaultMessage: string
    }

    interface Props {
        selectedRunningReportKey: string
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "ReportLog",
        components: {
            ErrorInfo,
            RunningReportsList,
            runningReportDetails
        },
        props: {
            selectedRunningReportKey: {
                type: String
            }
        },
        data(): Data {
            return {
                reports: [],
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            showReports: function () {
                return this.reports.length > 0
            }
        },
        mounted() {
            this.getAllReports();
        },
        methods: {
            getAllReports() {
                this.reports = [];
                api.get('/reports/running/')
                    .then(({data}) => {
                        this.reports = data.data;
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching the running reports";
                    });
            }
        }
    })
</script>
