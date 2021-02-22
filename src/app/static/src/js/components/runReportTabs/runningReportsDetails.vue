<template>
    <div class="container">
        <div id="report-log">
            <div class="row pt-2">
                <div id="report-git-branch" v-if="reportLog.git_branch" class="col-sm-auto">
                    <div class="text-right">
                        <span>Github branch:</span>
                        <span class="font-weight-bold">
                            {{ reportLog.git_branch }}
                        </span>
                    </div>
                </div>
                <div id="report-git-commit" v-if="reportLog.git_commit" class="col-sm-auto">
                    <div class="text-right">
                        <span>Github commit:</span>
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
                        <div class="d-md-table-row row" v-for="(n, index) in paramSize">
                            <span class="border border-secondary p-1">{{ reportLog.params[index].name }}:</span>
                            <span class="border border-secondary p-1">
                                {{ reportLog.params[index].value }}
                            </span>
                        </div>
                    </span>
                </div>
                <div id="report-database-source" v-if="instanceSize > 0" class="col-sm-auto">
                    <span>Database(source):</span>
                    <span>
                        <ul class="d-md-table-row list-unstyled" v-for="(n, index) in instanceSize">
                            <li class="p-1 font-weight-bold">{{ reportLog.instances[index].source }}</li>
                        </ul>
                    </span>
                </div>
                <div id="report-database-instance" v-if="instanceSize > 0" class="col-sm-auto">
                    <span>Database(annexe):</span>
                    <span>
                        <ul class="d-md-table-row list-unstyled" v-for="(n, index) in instanceSize">
                            <li class="p-1 font-weight-bold">{{ reportLog.instances[index].annexe }}</li>
                        </ul>
                    </span>
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
                        <span class="font-weight-bold"><a :href="versionUrl">{{ reportLog.report_version }}</a></span>
                    </div>
                </div>
            </div>
            <div id="report-logs" v-if="reportLog.logs" class="row pt-2">
                <div class="text-right col-sm-10">
                        <textarea class="form-control bg-white"
                                  readonly rows="10">{{ reportLog.logs }}
                        </textarea>
                </div>
            </div>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {ReportLog} from "../../utils/types";
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";

    interface Methods {
        getMetadata: () => void
    }

    interface Data {
        reportLog: ReportLog
        error: string,
        defaultMessage: string
    }

    interface Computed {
        paramSize: number
        instanceSize: number
        versionUrl: string
    }

    interface Props {
        reportKey: string
    }

    const initialReportLog = {
        email: "",
        date: "",
        report: "",
        instances: {},
        params: {},
        git_branch: "",
        git_commit: "",
        status: "",
        logs: "",
        report_version: ""
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "runningReportsDetails",
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
                reportLog: initialReportLog,
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            paramSize: function () {
                return Object.keys(this.reportLog.params).length
            },
            instanceSize: function () {
                return Object.keys(this.reportLog.instances).length
            },
            versionUrl: function () {
                return `/report/${this.reportLog.report}/${this.reportLog.report_version}/`
            }
        },
        methods: {
            getMetadata: function () {
                api.get(`/running/${this.reportKey}/logs/`)
                    .then(({data}) => {
                        this.reportLog = data.data
                        this.reportLog.params = JSON.parse(this.reportLog.params)
                        this.reportLog.instances = JSON.parse(this.reportLog.instances)
                        this.error = "";
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
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
                if(this.reportLog) {
                    this.getMetadata()
                }
            }
        }
    })
</script>
