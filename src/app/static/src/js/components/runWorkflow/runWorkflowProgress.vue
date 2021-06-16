<template>
    <div class="container mt-3" id="workflow-progress-container" v-if="workflowRunSummaries">
        <div class="row mb-3">
            <label for="workflows" class="form-label col">Workflow</label>
            <div class="col-10 px-0">
                <v-select
                    :options="workflowRunSummaries.data"
                    label="name"
                    :reduce="(label) => label.key"
                    name="workflows"
                    id="v-select"
                    v-model="selectedWorkflowKey"
                    placeholder="Search by name..."
                >
                    <template #option="{ name, email, date }">
                        <div>
                            {{ name }}
                            <span style="opacity: 0.5; float: right">
                                {{ email }} | {{ formatDate(date) }}</span
                            >
                        </div>
                    </template>
                </v-select>
            </div>
        </div>
        <div class="row" v-if="workflowRunStatus" id="workflow-table">
            <label class="form-label col">Reports</label>
            <table class="table-bordered col-10">
                <tr v-for="report in workflowRunStatus.data.reports">
                    <td v-if="report.status === 'success'">
                        <a
                            :href="runReportHref(report.name)"
                            @click="setRunReportTab"
                            >{{ report.name }}</a
                        >
                    </td>
                    <td v-else-if="report.status === 'error'">
                        <a
                            :href="reportLogsHref(report.name)"
                            @click="setReportLogsTab"
                            >{{ report.name }}</a
                        >
                    </td>
                    <td v-else>{{ report.name }}</td>
                    <td :style="{ color: statusColour(report.status) }">
                        {{ report.status }}
                    </td>
                    <td v-if="report.date">{{ formatDate(report.date) }}</td>
                </tr>
            </table>
        </div>
        <div class="row justify-content-end mt-3">
            <button class="button mr-3" type="button" disabled>
                Clone workflow
            </button>
            <button class="btn btn-grey" type="button" disabled>
                Cancel workflow
            </button>
        </div>
        <error-info
            :default-message="defaultMessage"
            :api-error="error"
        ></error-info>
    </div>
    <p v-else>No workflows to show</p>
</template>

<script lang="ts">
import Vue from "vue";
import vSelect from "vue-select";
import { api } from "../../utils/api";
import { longTimestamp } from "../../utils/helpers";
import ErrorInfo from "../errorInfo.vue";
import {
    RunWorkflowMetadata,
    WorkflowRunSummary,
    WorkflowRunStatus,
} from "../../utils/types";
import { buildFullUrl } from "../../utils/api";
import { session } from "./../../utils/session.js";

interface Data {
    workflowRunSummaries: null | WorkflowRunSummary[];
    selectedWorkflowKey: null | string;
    workflowRunStatus: null | WorkflowRunStatus;
    error: string;
    defaultMessage: string;
}

interface Methods {
    getWorkflowRunSummaries: () => void;
    getWorkflowRunStatus: (key: string) => void;
    formatDate: (date: string) => string;
    runReportHref: (name: string) => string;
    reportLogsHref: (name: string) => string;
    statusColour: (status: string) => string;
    setRunReportTab: () => void;
    setReportLogsTab: () => void;
}

interface Props {
    workflowMetadata: RunWorkflowMetadata | null;
}

export default Vue.extend<Data, Methods, unknown, Props>({
    name: "runWorkflowProgress",
    components: {
        ErrorInfo,
        vSelect,
    },
    props: {
        workflowMetadata: null,
    },
    data() {
        return {
            workflowRunSummaries: null,
            selectedWorkflowKey: null,
            workflowRunStatus: null,
            error: "",
            defaultMessage: "",
        };
    },
    methods: {
        getWorkflowRunSummaries() {
            api.get("/workflows")
                .then(({ data }) => {
                    this.workflowRunSummaries = data;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching the workflows";
                });
        },
        getWorkflowRunStatus(key) {
            api.get(`/workflows/${key}/status`)
                .then(({ data }) => {
                    this.workflowRunStatus = data;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching the workflow reports";
                });
        },
        formatDate(date) {
            return longTimestamp(new Date(date));
        },
        runReportHref(name) {
            const url = `/run-report?report-name=${name}`;
            return buildFullUrl(url);
        },
        reportLogsHref(name) {
            const url = `/run-report?report-name=${name}`; // this will eventually be changed to link to report logs once vimc-4618 has been implemented
            return buildFullUrl(url);
        },
        statusColour(status) {
            let colour = "";
            switch (status) {
                case "error":
                    colour = "red";
                    break;
                case "running":
                    colour = "grey";
                    break;
            }
            return colour;
        },
        setRunReportTab() {
            session.setSelectedTab("runReport");
        },
        setReportLogsTab() {
            session.setSelectedTab("reportLogs");
        },
    },
    watch: {
        selectedWorkflowKey() {
            if (this.selectedWorkflowKey) {
                this.getWorkflowRunStatus(this.selectedWorkflowKey);
            } else this.workflowRunStatus = null;
        },
    },
    mounted() {
        this.getWorkflowRunSummaries();
    },
});
</script>