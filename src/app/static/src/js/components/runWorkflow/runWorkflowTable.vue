<template>
    <table class="table-bordered">
        <tr>
            <th class="p-2">
                Report
            </th>
            <th v-if="anyParams" class="p-2" id="table-params-header">
                Parameters
            </th>
            <th class="p-2">
                Status
            </th>
            <th class="p-2">
                Logs
            </th>
        </tr>
        <tr v-for="(report, index) in workflowRunStatus.reports" :key="report.key">
            <td v-if="report.status === 'success'" class="p-2 row-name">
                <a class="report-version-link" :href="reportVersionHref(report.name, report.version)">
                    {{ report.name }}
                </a>
            </td>
            <td v-else class="p-2 row-name">
                {{ report.name }}
            </td>
            <td v-if="anyParams" class="p-2 tableParams">
                <run-workflow-parameters :report="workflowSummary.reports[index]"
                                         :report-index="index"></run-workflow-parameters>
            </td>
            <td :class="statusColour(report.status)" class="p-2">
                {{ interpretReportStatus(report.status) }}
            </td>
            <td v-if="report.date" class="p-2">
                {{ formatDate(report.date) }}
            </td>
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
    import {WorkflowRunStatus, WorkflowSummaryResponse} from "../../utils/types";
    import {buildFullUrl} from "../../utils/api";
    import {formatDate, runningReportStates, interpretReportStatus, hasParams} from "../../utils/helpers.ts";

    interface Props {
        workflowRunStatus: WorkflowRunStatus
        workflowSummary: WorkflowSummaryResponse | null
    }

    interface Methods {
        reportVersionHref: (name: string, version: string) => string;
        statusColour: (status: string) => string;
        viewLogLinkVisible: (status: string) => boolean;
        interpretReportStatus: (status: string) => string;
        formatDate: (date: string) => string;
    }

    interface Computed {
        anyParams: boolean
    }


    export default Vue.extend<unknown, Methods, Computed, Props>({
        name: "RunWorkflowTable",
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
            },
        },
        methods: {
            reportVersionHref(name, version) {
                const url = `/report/${name}/${version}/`;
                return buildFullUrl(url);
            },
            statusColour(status) {
                if (["queued", "running"].includes(status)) {
                    return "text-secondary";
                } else if (runningReportStates.failStates.includes(status)) {
                    return "text-danger";
                } else {
                    return "";
                }
            },
            viewLogLinkVisible(status) {
                return !runningReportStates.notStartedStates.includes(status);
            },
            interpretReportStatus(status) {
                return interpretReportStatus(status)
            },
            formatDate(date) {
                return formatDate(date)
            }
        },
    })
</script>