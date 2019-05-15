<template>
    <div id="run-report" class="mt-5">
        <label class="font-weight-bold">Run</label>
        <div>
            <div>Run this report to create a new version.</div>
            <div id="run-report-confirm" v-bind:class="['modal-background', {'modal-hide':!showModal}, {'modal-show':showModal}]">
                <div class="modal-main px-3 py-3">
                    <div class="mb-2 font-weight-bold">Confirm run report</div>
                    <div class="mb-2">Are you sure you want to run this report?</div>
                    <div class="modal-buttons">
                        <button v-on:click="run" id="confirm-run-btn" class="btn submit mr-3">Yes</button>
                        <button v-on:click="cancelRun" id="cancel-run-btn" class="btn btn-default">No</button>
                    </div>
                </div>
            </div>
        </div>
        <button v-on:click="confirmRun" class="btn mt-2" type="submit">Run report</button>
        <div id="run-report-status" v-if="runningStatus" class="text-secondary mt-2">
            Running status: {{runningStatus}}
            <div v-if="newVersionFromRun" id="run-report-new-version">
                New version: <a v-bind:href="`/reports/${report.name}/${newVersionFromRun}`">{{newVersionDisplayName}}</a>
            </div>
            <div v-on:click="dismissRunStatus" id="run-report-dismiss" class="btn btn-link p-0">Dismiss</div>
        </div>
    </div>
</template>

<script>
    import {reportVersionToLongTimestamp} from "../../helpers";
    import {session} from "../../session";
    import axios from "axios";
    export default {
        name: 'runReport',
        props: ['report'],
        data() {
            return {
                showModal: false,
                pollingTimer: null,
                runningKey: "",
                runningStatus: "",
                newVersionFromRun: "",
                newVersionDisplayName: ""
            }
        },
        mounted() {
            //check if we already have a runningStatusReport for this report in the session, and start monitoring it
            const existingStatus = session.getRunningReportStatus(this.report.name);

            this.runningStatus = existingStatus.runningStatus;
            this.runningKey = existingStatus.runningKey;
            this.newVersionFromRun = existingStatus.newVersionFromRun;
            this.newVersionDisplayName = existingStatus.newVersionDisplayName;

            if (this.runningStatus && !this.runHasCompleted()) {
                this.startPolling();
            }
        },
        methods: {
            confirmRun: function () {
                this.showModal = true;
            },
            cancelRun: function () {
                this.showModal = false;
            },
            run: function () {
                this.showModal = false;
                axios.post(`/reports/${this.report.name}/run/`,
                    null,
                    {withCredentials: true})
                    .then((response) => {
                        this.runningKey = response.data.data.key;
                        this.runningStatus = "Run started";

                        this.startPolling();
                    })
                    .catch(() => {
                        this.dismissRunStatus();
                        this.runningStatus = "Error when running report";
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
                        axios.get(`/reports/${this.runningKey}/status/`,
                            {withCredentials: true})
                            .then((response) => {
                                this.runningStatus = response.data.data.status;
                                this.newVersionFromRun = response.data.data.version;
                                this.newVersionDisplayName = this.newVersionFromRun ?
                                    reportVersionToLongTimestamp(this.newVersionFromRun) :
                                    "";

                                if (this.runHasCompleted()) {
                                    //Run has completed, successfully or not
                                    this.stopPolling();
                                }
                            })
                            .catch(() => {
                                this.runningStatus = "Error when fetching report status";
                            })
                            .finally(() => {
                                this.updateSessionStorage();
                            });
                    },
                    1500);
            },
            runHasCompleted: function () {
                return this.runningStatus === "success" || this.runningStatus === "error";
            },
            stopPolling: function () {
                if (this.pollingTimer) {
                    clearInterval(this.pollingTimer);
                    this.pollingTimer = null;
                }
            },
            dismissRunStatus: function () {
                this.stopPolling();
                this.runningKey = "";
                this.runningStatus = "";
                this.newVersionFromRun = "";
                this.newVersionDisplayName = "";
                this.updateSessionStorage();
            },
            updateSessionStorage: function () {
                if (this.runningStatus) {
                    session.setRunningReportStatus(this.report.name, {
                        runningStatus: this.runningStatus,
                        runningKey: this.runningKey,
                        newVersionFromRun: this.newVersionFromRun,
                        newVersionDisplayName: this.newVersionDisplayName
                    });
                }
                else {
                    session.removeRunningReportStatus(this.report.name);
                }
            }
        }
    }
</script>