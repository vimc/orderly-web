<template>
    <div>
        <form v-if="reports.length" class="mt-3">
            <div class="form-group row">
                <label class="col-sm-2 col-form-label text-right" for="report">Show logs for:</label>
                <div class="col-sm-10">
                    <select id="report" v-model="reportKey" class="form-control">
                        <option v-for="report in reports" :value="report.key">
                            {{ report.name }} Started on: {{ longTimestamp(new Date(report.date)) }}
                        </option>
                    </select>
                </div>
            </div>
        </form>
        <p v-else>You don't currently have any reports running</p>
        <template v-if="report">
            <div v-if="report.instance" class="form-group row">
                <label class="col-sm-2 col-form-label text-right" for="instance">Database instance:</label>
                <div class="col-sm-10">
                    <input id="instance" :value="report.instance" class="form-control-plaintext" readonly type="text">
                </div>
            </div>
            <div v-if="report.git_branch" class="form-group row">
                <label class="col-sm-2 col-form-label text-right" for="git-branch">Git branch:</label>
                <div class="col-sm-10">
                    <input id="git-branch" :value="report.git_branch" class="form-control-plaintext" readonly type="text">
                </div>
            </div>
            <div v-if="report.git_commit" class="form-group row">
                <label class="col-sm-2 col-form-label text-right" for="git-commit">Git commit:</label>
                <div class="col-sm-10">
                    <input id="git-commit" :value="report.git_commit" class="form-control-plaintext" readonly type="text">
                </div>
            </div>
        </template>
        <template v-if="reportStatus">
            <div class="form-group row">
                <label class="col-sm-2 col-form-label text-right" for="status">Status:</label>
                <div class="col-sm-10">
                    <input id="status" :value="reportStatus.status" class="form-control-plaintext" readonly type="text">
                </div>
            </div>
            <div v-if="reportStatus.status === 'success'" class="form-group row">
                <label class="col-sm-2 col-form-label text-right">Report version:</label>
                <div class="col-sm-10">
                    <a :href="reportHref" class="form-control-plaintext">{{ reportStatus.version }}</a>
                </div>
            </div>
            <div class="form-group row">
                <label class="col-sm-2 col-form-label text-right" for="output">Output:</label>
                <div class="col-sm-10">
                    <textarea id="output" class="form-control-plaintext text-monospace" cols="80" readonly rows="20">{{ reportStatus.output.stderr.join("\n") }}</textarea>
                </div>
            </div>
        </template>
    </div>
</template>

<script>
import {session} from "../../utils/session";
import {api} from "../../utils/api";
import {longTimestamp} from "../../utils/helpers";

export default {
    name: "reportLogs",
    data() {
        return {
            reports: [],
            reportKey: null,
            reportStatus: null,
            pollingTimer: null
        }
    },
    computed: {
        reportHref() {
            return `${api.baseUrl}/report/${this.report.name}/${this.reportStatus.version}`
        },
        report() {
            return this.reports.find(e => e.key === this.reportKey);
        }
    },
    mounted() {
        this.reports = session.getRunningReports(); //TODO get this from server rather than session
        this.reportKey = this.reports.length && this.reports[0].key;
    },
    methods: {
        startPolling: function () {
            if (this.pollingTimer) {
                this.stopPolling();
            }

            this.pollingTimer = setInterval(() => {
                    api.get(`/report/${this.report.name}/actions/status/${this.report.key}/?output=true`)
                        .then(({data}) => {
                            this.reportStatus = data.data;
                            if (this.reportStatus.status === "success") {
                                this.stopPolling();
                            }
                        })
                        .catch(({response}) => {
                            this.stopPolling();
                            console.log(response.data)
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
        longTimestamp
    },
    watch: {
        reportKey(key) {
            if (!key) {
                return;
            }
            api.get(`/report/${this.report.name}/actions/status/${this.report.key}/?output=true`)
                .then(({data}) => {
                    this.reportStatus = data.data;
                    this.startPolling();
                })
                .catch((error) => {
                    console.log(error);
                });
        }
    }
}
</script>
