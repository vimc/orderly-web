<template>
    <div>
        <h2>Running report logs</h2>
        <div v-if="showReports" id="logs-form-group" class="form-group row">
            <label for="report" class="col-sm-2 col-form-label text-right">Show logs for</label>
            <div class="col-sm-6">
                <report-list id="logs" 
                :reports="reports" 
                :report.sync="selectedReport"
                v-on="$listeners"
                :key.sync="selectedLogReportKey"/>
            </div>
        </div>
        <div v-else>
            <p id="noReportsRan">No reports have been ran yet</p>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import ReportList from "../runReport/reportList.vue";
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
        selectedLogReportKey: string,
        selectedReport: string,
        error: string,
        defaultMessage: string
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        components: {
            ErrorInfo,
            ReportList
        },
        data(): Data {
            return {
                reports: [],
                selectedLogReportKey: "",
                selectedReport: "",
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
                api.get('/running-reports/')
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
        mounted(){
            this.getAllReports();
        }
    })
</script>
