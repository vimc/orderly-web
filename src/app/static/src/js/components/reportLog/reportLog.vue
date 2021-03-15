<template>
<div>
    <div v-if="reportLogsEnabled">
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
                    id="logs-refresh-btn2"
                    class="btn col-sm-1"
                    :disabled="logsRefreshing"
                    type="submit">
                    {{refreshLogsText}}
            </button>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
    <div v-else>Report logs coming soon</div>
</div>
</template>

<script lang="ts">
    import Vue from "vue"
    import ReportList from "../runReport/reportList.vue";
    import {api} from "../../utils/api";
    // import EventBus from './../../eventBus';
    import ErrorInfo from "../errorInfo.vue";
    // import {switches} from "./../../featureSwitches";

    // interface Props {
    //     logsSelected: boolean
    // }

    interface Computed {
        refreshLogsText: string
        showReports: boolean
    }

    interface Methods {
        getAllReports: () => void
    }

    interface Data {
        reports: [],
        reportLogsEnabled: boolean,
        logsRefreshing: boolean,
        selectedLogReportKey: string,
        selectedReport: string,
        error: string,
        defaultMessage: string
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        components: {
            ReportList
        },
        props: [
            "logsSelected"
        ],
        data(): Data {
            return {
                reports: [],
                // reportLogsEnabled: switches.reportLog,
                reportLogsEnabled: true,
                logsRefreshing: false,
                selectedLogReportKey: "",
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
            this.getAllReports();
            // EventBus.$on("ranReport", function (payload) {
            //     console.log('emitted received in reportLog')
            //     this.getAllReports()
            // });

                console.log('logsSelected mounted', this.logsSelected)
        },
        watch: {
            selectedLogReportKey(){
                console.log('selectedLogReportKey in reportLog', this.selectedLogReportKey)
            },
            logsSelected() {
                console.log('logsSelected watch', this.logsSelected)
            }
        }
    })
</script>
