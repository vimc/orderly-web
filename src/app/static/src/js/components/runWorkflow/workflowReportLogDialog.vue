<template>
    <div id="workflow-report-log-container"
         v-bind:class="['modal-background', {'modal-hide':!show}, {'modal-show':show}]">
        <div class="modal-main px-3 py-3" style="min-width: 50em;">
            <div class="modal-header">
                <h5 class="modal-title">Report Log</h5>
            </div>
            <running-report-details
                    v-if="show"
                    :report-key="reportKey"
                    :workflow-key="workflowKey"
                    class="px-3"
            ></running-report-details>
            <div class="modal-buttons">
                <button
                    @click="$emit('close')"
                    id="workflow-report-log-close"
                    type="submit"
                    class="btn mt-3 px-4"
                >OK</button>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import RunningReportDetails from "../reportLog/runningReportDetails";

    interface Props {
        workflowKey: string | null
        reportKey: string | null
    }

    interface Computed {
        show: boolean
    }

    export default Vue.extend<unknown, unknown, Computed, Props>({
        name: "workflowReportLogDialog",
        props: {
            workflowKey: {
                type: String,
                required: false
            },
            reportKey: {
                type: String,
                required: false
            }
        },
        computed: {
            show() {
                return !!this.workflowKey && !!this.reportKey;
            }
        },
        components: {
            RunningReportDetails
        }
    });
</script>
