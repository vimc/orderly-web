<template>
    <!-- <p>Run workflow progress is coming soon</p> -->
    <div class="container mt-3" v-if="workflowRunSummaries">
        <div class="row mb-3">
            <label for="workflows" class="form-label col">Workflow</label>
            <div class="col-10 px-0">
                <!-- <v-select 
                    :options="workflowOptions" 
                    label="label" 
                    :reduce="label => label.key" 
                    name="workflows" 
                    id="workflows" 
                    v-model="selectedWorkflowKey">
                </v-select> -->
                <v-select
                    :options="workflowRunSummaries.data"
                    label="name"
                    :reduce="(label) => label.key"
                    name="workflows"
                    id="workflows"
                    v-model="selectedWorkflowKey"
                    placeholder="Search by name..."
                >
                    <template #option="{ name, email, date }">
                        <!-- <span class="col-4">{{ option.name }}</span>
                            <span class="col-4">Started: {{ formatDate(option.date) }}</span> -->
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
        <!-- <div class="row mb-3">
            <label for="workflows" class="form-label col">Workflow</label>
            <select name="workflows" id="workflows" class="form-select col-10" v-model="selectedWorkflowKey">
                <option v-for="workflow in workflowRunSummaries.data" :value="workflow.key">
                    <span >{{ workflow.name }}</span>
                    <span>Started: {{ formatDate(workflow.date) }}</span>
                </option>
            </select>
        </div> -->
        <div class="row" v-if="workflowRunStatus">
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
</template>

<script lang="ts">
import Vue from "vue";
import vSelect from "vue-select";
import { api } from "../../utils/api";
import { longTimestamp } from "../../utils/helpers";
import ErrorInfo from "../errorInfo.vue";
import { RunWorkflowMetadata } from "../../utils/types";
import { buildFullUrl } from "../../utils/api";
import { session } from "./../../utils/session.js";

// interface Data {
//     workflowRunSummaries: null,
//     selectedWorkflowKey: null,
//     workflowRunStatus: null,
//     error: string,
//     defaultMessage: string
// }

interface Props {
    workflowMetadata: RunWorkflowMetadata | null;
}

export default Vue.extend<unknown, unknown, unknown, Props>({
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
    computed: {
        workflowOptions() {
            const options = [];
            if (this.workflowRunSummaries.status === "success") {
                this.workflowRunSummaries.data.forEach((workflow) =>
                    options.push({
                        label: `${workflow.name} Started: ${this.formatDate(
                            workflow.date
                        )}`,
                        key: workflow.key,
                    })
                );
            }
            return options;
        },
    },
    methods: {
        getWorkflowRunSummaries() {
            api.get("/workflows")
                .then(({ data }) => {
                    console.log(data);
                    this.workflowRunSummaries = data;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    console.log(error);
                    this.error = error;
                    this.defaultMessage =
                        "An error occurred fetching the workflows";
                });
        },
        getWorkflowRunStatus(key) {
            api.get(`/workflows/${key}/status`)
                .then(({ data }) => {
                    console.log("details", data);
                    this.workflowRunStatus = data;
                    this.error = "";
                    this.defaultMessage = "";
                })
                .catch((error) => {
                    console.log(error);
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
                // default:
                //     return ""
            }
            return colour;
        },
        setRunReportTab() {
            session.setSelectedTab("runReport");
        },
        setReportLogsTab() {
            session.setSelectedTab("reportLogs");
        }
    },
    watch: {
        selectedWorkflowKey() {
            if (this.selectedWorkflowKey) {
                console.log(this.selectedWorkflowKey);
                this.getWorkflowRunStatus(this.selectedWorkflowKey);
            } else this.workflowRunStatus = null;
        },
    },
    mounted() {
        this.getWorkflowRunSummaries();
    },
});
</script>