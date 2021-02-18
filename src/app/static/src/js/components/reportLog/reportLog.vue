<template>
<div>
    <div v-if="showReports" id="report-form-group" class="form-group row">
        <label for="report" class="col-sm-2 col-form-label text-right">Show logs for</label>
        <div class="col-sm-6">
            <report-list id="report" :reports="reports" :report.sync="selectedReport"/>
        </div>
    </div>
</div>
</template>

<script lang="ts">
    import Vue from "vue"
    import ReportList from "../runReport/reportList.vue";
    import {ReportLog} from "../../utils/types";
    import {api} from "../../utils/api";

    interface Computed {
        showReports: boolean
    }

    interface Methods {
        getAllReports: () => void
        getMetadata: () => void
    }

    interface Data {
        reports: [],
        selectedReport: string,
        reportLog: ReportLog,
        error: string,
        defaultMessage: string,
        reportId: string
    }

    const initialReportLog = {
        email: "",
        date: "",
        report: "",
        instances: "",
        params: "",
        gitBranch: "",
        gitCommit: "",
        status: "",
        log: "",
        reportVersion: ""
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        // props: [
        //     "metadata"
        // ],
        components: {
            ReportList
        },
        data(): Data {
            return {
                reports: [],
                reportId: "",
                selectedReport: "",
                reportLog: initialReportLog,
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            showReports: function () {
                return true
            }
        },
        methods: {
            getAllReports() {
                this.reports = [];
                // const user = `?user=${this.userEmail}`;
                api.get('/running/')
                    .then(({data}) => {
                        this.reports = data.data;
                        this.error = "";
                        this.defaultMessage = "";
                        console.log('these are the running reports', data.data)
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching reports";
                    });
            },
            getMetadata: function() {
                api.get(`/report/${this.key}/logs`)
                    .then(({data}) => {
                        this.reportLog = data.data
                        this.error = "";
                        this.defaultMessage = "";
                        console.log('this is the key', this.key)
                        console.log('this is the metadata 2', this.reportLog)
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred when fetching metadata";
                    });
            }
        },
        mounted(){
            // console.log('this is the metadata 1', this.metadata)
            // this.updateReports();
            // console.log('theres are the keys')
            this.getAllReports()
        },
        watch: {
            'reportLog.status': {
                handler() {
                    this.getMetadata()
                },
                deep: true
            }
        }
    })
</script>
