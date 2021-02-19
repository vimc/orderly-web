<template>
<div>
    <div v-if="showReports" id="report-form-group" class="form-group row">
        <label for="report" class="col-sm-2 col-form-label text-right">Show logs for</label>
        <div class="col-sm-6">
            <report-list id="report" 
            :reports="reports" 
            :report.sync="selectedReport" 
            :key.sync="selectedReportKey"/>
        </div>
        <button @click.prevent="getAllReports"
                id="logs-refresh-btn"
                class="btn col-sm-1"
                :disabled="logsRefreshing"
                type="submit">
                {{refreshLogsText}}
        </button>
    </div>
    <div v-else>
        <div>No reports have been ran yet</div>
        <button @click.prevent="getAllReports"
                id="logs-refresh-btn"
                class="btn col-sm-1"
                :disabled="logsRefreshing"
                type="submit">
                {{refreshLogsText}}
        </button>
    </div>
    <error-info :default-message="defaultMessage" :api-error="error"></error-info>
</div>
</template>

<script lang="ts">
    import Vue from "vue"
    import ReportList from "../runReport/reportList.vue";
    import {api} from "../../utils/api";
    import EventBus from './../../eventBus';
    import ErrorInfo from "../errorInfo.vue";

    interface Computed {
        refreshLogsText: string
        showReports: boolean
    }

    interface Methods {
        getAllReports: () => void
    }

    interface Data {
        reports: [],
        logsRefreshing: boolean,
        selectedReportKey: string,
        selectedReport: string,
        error: string,
        defaultMessage: string
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        components: {
            ReportList
        },
        data(): Data {
            return {
                reports: [],
                logsRefreshing: false,
                selectedReportKey: "",
                selectedReport: "",
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            refreshLogsText(){
                return this.logsRefreshing ? 'Fetching...' : 'Refresh'
            },
            showReports: function () {
                return this.reports.length > 0
            }
        },
        methods: {
            getAllReports() {
                this.logsRefreshing = true
                this.reports = [];
                api.get('/running/')
                    .then(({data}) => {
                        this.logsRefreshing = false;
                        this.reports = data.data;
                        this.error = "";
                        this.defaultMessage = "";
                        console.log('get all reports fired', data.data)
                    })
                    .catch((error) => {
                        this.logsRefreshing = false;
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching the running reports";
                    });
            }
        },
        mounted(){
            // setInterval(this.getAllReports(), 3000);
            this.getAllReports();
            EventBus.$on("ranReport", function (payload) {
                console.log('emitted received in reportLog')
                this.getAllReports()
            });
        },
        watch: {
            selectedReportKey() {
                console.log(this.selectedReportKey)
            }
        }
    })
</script>
