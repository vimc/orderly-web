<template>
    <div id="run-report" class="mt-5">
        <label class="font-weight-bold">Run</label>
        <div>
            <div>Run this report to create a new version.</div>
        </div>
        <a @click="emitReportName" :href="runReportHref">Run report</a>
        <div id="run-report-status" v-if="runningStatus" class="text-secondary mt-2">
            Running status: {{runningStatus}}
            <div v-if="runHasCompleted && newVersionFromRun" id="run-report-new-version">
                New version: <a v-bind:href="newVersionHref">{{newVersionDisplayName}}</a>
            </div>
            <button @click="dismissRunStatus" id="run-report-dismiss" class="btn btn-link">Dismiss</button>
        </div>
    </div>
</template>

<script>
    import {reportVersionToLongTimestamp} from "../../utils/helpers";
    import {session} from "../../utils/session";
    import {api} from "../../utils/api"

    const initialState = {
        showModal: false,
        pollingTimer: null,
        runningKey: "",
        runningStatus: "",
        newVersionFromRun: null
    };

    export default {
        name: 'runReport',
        props: ['report'],
        data() {
            return {
                ...initialState
            }
        },
        mounted() {
            //check if we already have a runningStatusReport for this report in the session, and start monitoring it
            const existingStatus = session.getRunningReportStatus(this.report.name);
            Object.assign(this, existingStatus);

            if (this.runningStatus && !this.runHasCompleted) {
                this.startPolling();
            }
        },
        computed: {
            newVersionDisplayName: function () {
                return this.newVersionFromRun ? reportVersionToLongTimestamp(this.newVersionFromRun) : "";
            },
            runHasCompleted: function () {
                return this.runningStatus && (this.runningStatus === "success" || this.runningStatus.toLowerCase().indexOf("error") > -1);
            },
            newVersionHref: function() {
                return `${api.baseUrl}/report/${this.report.name}/${this.newVersionFromRun}`
            },
            runReportHref: function () {
                return `/run-report?name=${this.report.name}`
            },
        },
        watch: {
            runningStatus: function() {
                if (this.runHasCompleted) {
                    this.stopPolling();
                }
            }
        },
        methods: {
            emitReportName: function () {
                //eventHub.$emit("emitted-report-name", this.report.name)
            },
            run: function () {
                api.post(`/report/${this.report.name}/actions/run/`)
                    .then(({data}) => {
                        this.runningKey = data.data.key;
                        this.runningStatus = "Run started";
                        this.startPolling();
                    })
                    .catch(({response}) => {
                        this.dismissRunStatus();
                        this.runningStatus = "Error when running report";
                        console.log(response.data);
                    })
                    .finally(() => {
                        this.updateSessionStorage();
                    });
            },
            startPolling: function () {
                if (this.pollingTimer) {
                    this.stopPolling();
                }

                this.pollingTimer = setInterval(() => {
                        api.get(`/report/${this.report.name}/actions/status/${this.runningKey}/`)
                            .then(({data}) => {
                                this.runningStatus = data.data.status;
                                this.newVersionFromRun = data.data.version;
                            })
                            .catch(({response}) => {
                                this.stopPolling();
                                this.runningStatus = "Error when fetching report status";
                                console.log(response.data)
                            })
                            .finally(() => {
                                this.updateSessionStorage();
                            });
                    },
                    1500);
            },
            stopPolling: function () {
                if (this.pollingTimer) {
                    clearInterval(this.pollingTimer);
                    this.pollingTimer = null;
                }
            },
            dismissRunStatus: function () {
                this.stopPolling();
                Object.assign(this, initialState);
                this.updateSessionStorage();
            },
            updateSessionStorage: function () {
                if (this.runningStatus) {
                    session.setRunningReportStatus(this.report.name, this);
                } else {
                    session.removeRunningReportStatus(this.report.name);
                }
            }
        }
    }
</script>