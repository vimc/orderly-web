<template>
    <div>
        <div>
            <div id="report-log">
                <div class="row pt-2">
                    <div v-if="reportLog.gitBranch" class="col-sm-auto">
                        <div class="text-right">
                            Github branch:
                            <b>{{ reportLog.gitBranch }}</b>
                        </div>
                    </div>
                    <div v-if="reportLog.gitCommit" class="col-sm-auto">
                        <div class="text-right">
                            Github commit:
                            <b>{{ reportLog.gitCommit }}</b>
                        </div>
                    </div>
                    <div v-if="reportLog.instances" class="col-sm-auto">
                        <div class="text-right">
                            Instance(s):
                            <b>{{ reportLog.instances }}</b>
                        </div>
                    </div>
                </div>
                <div class="row pt-2">
                    <div v-if="reportLog.status" class="col-sm-auto">
                        <div class="text-right">
                            Status:
                            <b>{{ reportLog.status }}</b>
                        </div>
                    </div>
                    <div v-if="reportLog.reportVersion" class="col-sm-auto">
                        <div class="text-right">
                            Report version:
                            <b>{{ reportLog.reportVersion }}</b>
                        </div>
                    </div>
                    <div v-if="reportLog.params" class="col-sm-auto">
                        <div class="text-right">
                            Parameters:
                            <b>{{ reportLog.params }}</b>
                        </div>
                    </div>
                </div>
                <div class="row pt-2">
                    <div v-if="reportLog.log" class="text-right col-sm-8">
                        <textarea class="form-control bg-white" readonly rows="10">
                            {{ reportLog.log }}
                        </textarea>
                    </div>
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

    interface Methods {
        getMetadata: () => void
    }

    interface Data {
        reportLog: ReportLog,
        error: string,
        defaultMessage: string
    }

    interface Props {
        reportKey: string
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

    export default Vue.extend<Data, Methods, unknown, Props>({
        name: "reportLog",
        props: {
            reportKey: {
                type: String,
                required: true
            }
        },
        components: {
            ReportList
        },
        data(): Data {
            return {
                reportLog: initialReportLog,
                error: "",
                defaultMessage: ""
            }
        },
        methods: {
            getMetadata: function () {
                api.get(`/report/${this.reportKey}/logs`)
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
            reportKey() {
                this.getMetadata()
            }
        }
    })
</script>
