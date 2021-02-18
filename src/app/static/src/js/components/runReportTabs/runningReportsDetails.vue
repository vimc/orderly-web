<template>
    <div>
        <div>
            <div id="report-log">
                <div class="row pt-2">
                    <div v-if="reportLog.git_branch" class="col-sm-auto">
                        <div class="text-right">
                            Github branch:
                            <b>{{ reportLog.git_branch }}</b>
                        </div>
                    </div>
                    <div v-if="reportLog.git_commit" class="col-sm-auto">
                        <div class="text-right">
                            Github commit:
                            <b>{{ reportLog.git_commit }}</b>
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
                    <div v-if="showParams" class="col-sm-auto">
                        <div class="text-right">
                            Parameters:
                            <b>{{ reportLog.params["0"].name }}: {{ reportLog.params["0"].value }}</b>
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
                    <div v-if="reportLog.report_version" class="col-sm-auto">
                        <div class="text-right">
                            Report version:
                            <b>{{ reportLog.report_version }}</b>
                        </div>
                    </div>
                </div>
                <div class="row pt-2">
                    <div class="text-right col-sm-8">
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
    import {Parameter, ReportLog} from "../../utils/types";
    import {api} from "../../utils/api";

    interface Methods {
        getMetadata: () => void
    }

    interface Data {
        reportLog: ReportLog,
        error: string,
        defaultMessage: string
    }

    interface Computed {
        showParams: boolean
    }

    interface Props {
        reportKey: string
    }

    const initialReportLog = {
        email: "test",
        date: "test",
        report: "test",
        instances: "test",
        params: {"0":{"name": "test name", "value": "test value"}},
        git_branch: "test",
        git_commit: "test",
        status: "test",
        log: "test",
        report_version: "test"
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
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
        computed: {
            showParams: function () {
                return this.reportLog.params && this.reportLog.params.length
            }
        },
        methods: {
            getMetadata: function () {
                api.get(`/reports/${this.reportKey}/logs`)
                    .then(({data}) => {
                        this.reportLog = data.data
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        console.log("Error occurred here")
                        this.error = error;
                        this.defaultMessage = "An error occurred when fetching metadata";
                    });
            }
        },
        mounted() {
            if (this.reportKey) {
                this.getMetadata()
            }
        },
        watch: {
            reportKey() {
                this.getMetadata()
            }
        }
    })
</script>
