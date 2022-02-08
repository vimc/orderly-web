<template>
    <div id="workflow-report-log-container" @click="backgroundClick"
         v-bind:class="['modal-background', {'modal-hide':!show}, {'modal-show':show}]">
        <div class="modal-main px-3" style="min-width: 50em;">
            <div class="modal-header">
                <h5 class="modal-title">Report Log</h5>
            </div>
            <running-report-details
                    v-if="show"
                    :report-key="reportKey"
                    :workflow-key="workflowKey"
                    class="px-3"
            ></running-report-details>
            <div class="modal-footer mt-4">
                <button
                    @click="$emit('close')"
                    id="workflow-report-log-close"
                    type="submit"
                    class="modal-buttons btn px-4"
            >Close</button>
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

    interface Methods {
        backgroundClick: (e: Event) => void
    }

    export default Vue.extend<unknown, Methods, Computed, Props>({
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
        methods: {
          backgroundClick(e: Event) {
              // Close only if click on background div
              if (e.target === e.currentTarget) {
                  this.$emit("close");
              }
          }
        },
        components: {
            RunningReportDetails
        }
    });
</script>
