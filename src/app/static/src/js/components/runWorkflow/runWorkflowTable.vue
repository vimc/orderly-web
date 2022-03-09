<template>
    <table class="table-bordered">
        <tr>
            <th class="p-2">Report</th>
            <th v-if="anyParams" class="p-2">Parameters</th>
            <th class="p-2">Status</th>
            <th class="p-2">Logs</th>
        </tr>
        <tr v-for="(report, index) in workflowRunStatus.reports" :key="report.key">
            <td v-if="report.status === 'success'" class="p-2">
                <a class="report-version-link" :href="reportVersionHref(report.name, report.version)">
                    {{ report.name }}
                </a>
            </td>
            <td v-else class="p-2">{{ report.name }}</td>
            <td v-if="anyParams" class="p-2">
                <run-workflow-parameters :report="workflowSummary.reports[index]"></run-workflow-parameters>
            </td>
            <td :class="statusColour(report.status)" class="p-2">
                {{ interpretStatus(report.status) }}
            </td>
            <td v-if="report.date" class="p-2">{{ formatDate(report.date) }}</td>
            <td class="p-2">
                <a v-if="viewLogLinkVisible(report.status)"
                    class="report-log-link"
                    href="#"
                    @click="$emit('show-report-log', report.key)">
                    View log
                </a>
            </td>
        </tr>
    </table>
</template>

<script lang="ts">
import Vue from 'vue'
import runWorkflowParameters from "./runWorkflowParameters.vue"
import {WorkflowRunReportStatus, WorkflowSummaryResponse} from "../../utils/types";
import {buildFullUrl} from "../../utils/api";
import {formatDate, failStates, notStartedStates, interpretStatus, hasParams} from "../../utils/helpers.ts";

interface Props {
    workflowRunStatus: WorkflowRunReportStatus
    workflowSummary: WorkflowSummaryResponse | null
}

interface Methods {
    reportVersionHref: (name: string, version: string) => string;
    statusColour: (status: string) => string;
    viewLogLinkVisible: (status: string) => boolean;
    interpretStatus: (status: string) => string;
}

interface Computed {
    anyParams: boolean
}


export default Vue.extend<unknown, Methods, Computed, Props>({
    name: "runWorkflowTable",
    components: {
        runWorkflowParameters
    },
    props: {
        workflowRunStatus: {
            type: Object,
            required: true
        },
        workflowSummary: {
            type: Object,
            default: null
        }
    },
    computed: {
        anyParams() {
            return !!this.workflowSummary?.reports.some(report => hasParams(report))
        }
    },
    methods: {
        reportVersionHref(name, version) {
            const url = `/report/${name}/${version}/`;
            return buildFullUrl(url);
        },
        statusColour(status) {
            if (["queued", "running"].includes(status)) {
                return "text-secondary";
            } else if (failStates.includes(status)) {
                return "text-danger";
            } else {
                return "";
            }
        },
        viewLogLinkVisible(status) {
            return !notStartedStates.includes(status);
        },
        interpretStatus(status){
            return interpretStatus(status)
        }
    },
    
})
</script>