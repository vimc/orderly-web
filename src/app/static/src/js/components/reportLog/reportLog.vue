<template>
    <div>
        <div v-if="showReports" id="report-form-group" class="form-group row">
            <label for="report" class="col-sm-2 col-form-label text-right">Show logs for</label>
            <div class="col-sm-6">
                <report-list id="report" :reports="report" :report.sync="selectedReport"/>
            </div>
        </div>
        <div v-if="showReportLog" id="report-log">
            <div class="form-group row">
                <div class="col-sm-2 text-right">Github branch:</div>
                <div class="col-auto font-weight-bold">{{ reportLog.gitBranch }}</div>
                <div class="text-right">Github commit:</div>
                <div class="col-auto font-weight-bold">{{ reportLog.gitCommit }}</div>
                <div>Instance(s):</div>
                <div class="col-auto font-weight-bold">{{ reportLog.instances }}</div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 text-right">Status:</div>
                <div class="col-auto font-weight-bold">{{ reportLog.status }}</div>
                <div class="text-right">Report version:</div>
                <div class="col-auto font-weight-bold">{{reportLog.reportVersion}}</div>
                <div class="text-right">Parameters:</div>
                <div class="col-auto font-weight-bold">{{ reportLog.params }}</div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 text-right"></div>
                <div class="col-sm-8 vh-100">
                <textarea class="form-control bg-white" readonly rows="10">
                {{ reportLog.log }}
               </textarea>
                </div>
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
        showReportLog: boolean
    }

    interface Methods {
        getMetadata: () => void
    }

    interface Data {
        selectedReport: string,
        reportLog: ReportLog,
        error: string,
        defaultMessage: string,
        reportId: string
        report,
    }

    const initialReportLog = {
        email: "test",
        date: "test",
        report: "test",
        instances: "test",
        params: "test",
        gitBranch: "test",
        gitCommit: "test",
        status: "test",
        log: "test",
        reportVersion: "test"
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        components: {
            ReportList
        },
        data(): Data {
            return {
                report: [],
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
            },
            showReportLog: function () {
                return true
            }
        },
        methods: {
            getMetadata: function() {
                api.get(`/report/${this.key}/logs`)
                    .then(({data}) => {
                        this.reportLog = data.data
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred when fetching metadata";
                    });
            }
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
