<template>
    <div>
        <div v-if="reportLog">
            <div id="report-log">
                <div class="row pt-2">
                    <div id="report-name" class="col-sm-auto ">
                        <span>Report:</span>
                        <span class="font-weight-bold">
                            {{ reportLog.report }}
                        </span>
                    </div>
                    <div id="report-start" class="col-sm-auto ">
                        <span>Run started:</span>
                        <span class="font-weight-bold">
                            {{ formattedReportDate }}
                        </span>
                    </div>
                </div>
                <div class="row pt-2">
                    <div id="report-git-branch" v-if="reportLog.git_branch" class="col-sm-auto">
                        <div class="text-right">
                            <span>Git branch:</span>
                            <span class="font-weight-bold">
                            {{ reportLog.git_branch }}
                        </span>
                        </div>
                    </div>
                    <div id="report-git-commit" v-if="reportLog.git_commit" class="col-sm-auto">
                        <div class="text-right">
                            <span>Git commit:</span>
                            <span class="font-weight-bold">
                            {{ reportLog.git_commit }}
                        </span>
                        </div>
                    </div>
                </div>
                <div class="row pt-2">
                    <div id="report-params" v-if="paramSize > 0" class="col-sm-auto">
                        <span>Parameters:</span>
                        <span>
                        <div class="d-md-table-row row" v-for="(value, key) in reportLog.params">
                            <span class="border border-secondary col-md">{{ key }}</span>
                            <span class="border border-secondary col-md">{{ value }}</span>
                        </div>
                    </span>
                    </div>
                </div>
                <div id="report-database-instances"  v-if="instanceSize > 0" class="row pt-2">
                    <div v-for="(value, key) in reportLog.instances"
                         class="report-database-instance col-sm-auto">
                        <span>Database "{{ key }}":</span>
                        <span class="font-weight-bold">{{ value }}</span>
                    </div>
                </div>
                <div class="row pt-2">
                    <div id="report-status" v-if="reportLog.status" class="col-sm-auto">
                        <div class="text-right">
                            <span>Status:</span>
                            <span class="font-weight-bold">{{ reportLog.status }}</span>
                        </div>
                    </div>
                    <div id="report-version" v-if="reportLog.report_version" class="col-sm-auto">
                        <div class="text-right">
                            <span>Report version:</span>
                            <span class="font-weight-bold"><a :href="versionUrl">{{reportLog.report_version}}</a></span>
                        </div>
                    </div>
                </div>
                <div id="report-logs" class="row pt-2">
                    <div class="text-right col-12">
                        <textarea ref="logs"
                                  class="form-control bg-white text-monospace" style="font-size: 80%;"
                                  readonly rows="20">{{ reportLog.logs }}
                        </textarea>
                    </div>
                </div>
            </div>
        </div>
        <div id="no-logs" v-if="!reportLog">There are no logs to display</div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {ReportLog} from "../../utils/types";
    import {api, buildFullUrl} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import {longTimestamp} from "../../utils/helpers.ts";

    interface Methods {
        getLogs: () => void,
        startPolling: () => void,
        stopPolling: () => void
    }

    interface Data {
        reportLog: ReportLog | null
        error: string,
        defaultMessage: string,
        pollingTimer: number | null
    }

    interface Computed {
        paramSize: number
        instanceSize: number
        versionUrl: string
        formattedReportDate: string
    }

    interface Props {
        reportKey: string
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runningReportDetails",
        props: {
            reportKey: {
                type: String,
                required: true
            }
        },
        components: {
            ErrorInfo
        },
        data(): Data {
            return {
                reportLog: null,
                error: "",
                defaultMessage: "",
                pollingTimer: null
            }
        },
        computed: {
            paramSize: function () {
                return Object.keys(this.reportLog.params).length
            },
            instanceSize: function () {
                return this.reportLog.instances && Object.keys(this.reportLog.instances).length
            },
            versionUrl: function () {
                const url = `/report/${this.reportLog.report}/${this.reportLog.report_version}/`
                return buildFullUrl(url)
            },
            formattedReportDate: function () {
                return longTimestamp(new Date(this.reportLog.date));
            }
        },
        methods: {
            getLogs: function () {
                if (this.reportKey) {
                    api.get(`/running/${this.reportKey}/logs/`)
                        .then(({data}) => {
                            this.reportLog = data.data;
                            this.error = "";
                            this.defaultMessage = "";

                            const status = this.reportLog.status;

                            this.$nextTick(() => {
                                this.$refs.logs.scrollTop = this.$refs.logs.scrollHeight;
                            });

                            if (status === "running" || status === "queued") {
                                this.startPolling();
                            }
                            else  {
                                this.stopPolling(); //the run has completed
                            }
                        })
                        .catch((error) => {
                            this.error = error;
                            this.defaultMessage = "An error occurred when fetching logs";
                        });
                }
            },
            startPolling: function () {
                if (!this.pollingTimer) {
                    this.pollingTimer = setInterval(this.getLogs, 1500);
                }
            },
            stopPolling: function () {
                if (this.pollingTimer) {
                    clearInterval(this.pollingTimer);
                    this.pollingTimer = null;
                }
            }
        },
        mounted() {
            this.getLogs();
        },
        watch: {
            reportKey() {
                this.stopPolling();
                this.getLogs();
            },
            reportLog(){
                if (this.reportLog){
                    api.get(`/reports/${this.reportLog.report}/latest/`)
                        .then(({data}) => {
                            console.log("data", data)
                        })
                        .catch((error) => {
                            console.log("error", error)
                        });

                }
            }
        }
    })
</script>
