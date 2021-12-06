<template>
    <div>
        <h2 id="summary-header">Summary</h2>
        <div id="summary-warning" class="mt-3">
            <div class="row">
                <div class="col-auto"><alert-triangle-icon size="2x" stroke="red" class="custom-class"/></div>
                <div class="col-auto">
                    <span class="d-inline-block pb-2"> Some reports depend on the latest version of other reports that are not included in your workflow:</span>
                    <div>
                        <span class="font-weight-bold">report name</span>
                        <ul class="styled">
                            <li>missing dependencies</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <hr class="pb-4">
        <div id="summary-timeline">
            <div class="row">
                <div class="col-12">
                    <div class="summary">
                        <div v-for="report in workflowMetadata.reports" class="single-timeline-area">
                            <div class="timeline-date wow fadeInLeft" data-wow-delay="0.1s" style="visibility: visible; animation-delay: 0.1s; animation-name: fadeInLeft;">
                            </div>
                            <div class="d-inline-block"><h5>{{ report.name }}</h5></div>
                            <div class="row">
                                <div class="col-12 col-md-6 col-lg-4">
                                    <div class="single-timeline-content parameters-bg-color d-flex wow fadeInLeft"
                                         data-wow-delay="0.3s"
                                         style="visibility: visible; animation-delay: 0.3s; animation-name: fadeInLeft;">
                                        <div class="timeline-text">
                                            <span class="text-muted d-inline-block">Parameters</span>
                                            <div v-if="hasParameters(report)">
                                                <p v-for="(value, key) in report.params">{{ key }} {{ value }}</p>
                                                <a href="#" class="pt-2 d-inline-block small">Show default...</a>
                                            </div>
                                            <div v-else><p>There are no parameters</p></div>
                                        </div>
                                    </div>
                                </div>
                                <span class="d-inline-block"></span>
                                <div class="col-12 col-md-6 col-lg-4">
                                    <div class="single-timeline-content d-flex wow fadeInLeft" data-wow-delay="0.5s"
                                         style="visibility: visible; animation-delay: 0.5s; animation-name: fadeInLeft;">
                                        <div class="timeline-text">
                                            <div class="pb-2">
                                                <span class="text-muted d-inline-block">Depends on</span>
                                                <p>Burden-report</p>
                                            </div>
                                            <div class="text-danger">
                                                <span class="d-inline-block">Missing dependencies:</span>
                                                <p>Touchstone</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {RunWorkflowMetadata, WorkflowReportWithParams} from "../../utils/types";
    import {AlertTriangleIcon} from "vue-feather-icons"

    interface Props {
        workflowMetadata: RunWorkflowMetadata;
    }

    interface Computed {
        reportCount: string;
    }

    interface Methods {
        hasParameters: (report: WorkflowReportWithParams) => boolean;
    }

    export default Vue.extend<unknown, Methods, Computed, Props>({
        name: "runWorkflowSummary",
        components: {
            AlertTriangleIcon
        },
        props: {
            workflowMetadata: {
                required: true,
                type: Object
            }
        },
        computed: {
            reportCount() {
                const num = this.workflowMetadata?.reports?.length;
                if (num == 1) {
                    return "1 report"
                } else {
                    return `${num} reports`
                }
            }
        },
        methods: {
            hasParameters(report) {
                return !!Object.keys(report.params).length
            }
        },
        mounted() {
            this.$emit("valid", true)
        }
    })
</script>
