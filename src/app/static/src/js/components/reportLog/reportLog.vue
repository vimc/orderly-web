<template>
    <div>
        <h2>Running report logs</h2>
        <div v-if="showReports" id="logs-form-group" class="form-group row">
            <label for="report" class="col-sm-2 col-form-label text-right">Show logs for</label>
            <div class="col-sm-6">
                <running-reports-list id="logs" 
                :reports="reports" 
                v-on="$listeners"
                :key.sync="selectedRunningReportKey"/>
            </div>
            <!-- runningReportDetails to go here and given selectedRunningReportKey as prop -->
        </div>
        <div v-else>
            <p id="noReportsRun">No reports have been run yet</p>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import RunningReportsList from "./runningReportsList.vue"
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";


    interface Computed {
        showReports: boolean
    }

    interface Methods {
        getAllReports: () => void
    }

    interface Data {
        reports: [],
        selectedRunningReportKey: string,
        error: string,
        defaultMessage: string
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        components: {
            ErrorInfo,
            RunningReportsList
        },
        data(): Data {
            return {
                reports: [],
                selectedRunningReportKey: "",
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            showReports: function () {
                return this.reports.length > 0
            }
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
        },
        watch: {
            selectedRunningReportKey(){
                console.log(this.selectedRunningReportKey)
            }
        },
        mounted(){
            this.getAllReports();
        }
    })
</script>
