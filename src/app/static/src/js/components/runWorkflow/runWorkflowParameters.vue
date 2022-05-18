<template>
    <div v-if="hasParams">
        <p v-for="(param, paramIndex) in report.param_list"
           :key="paramIndex"
           class="non-default-param">{{ param.name }}: {{ param.value }}</p>
        <p v-if="!report.param_list.length" class="no-non-default-params">
            No non-default parameters
        </p>
        <div v-if="report.default_param_list.length > 0"
             class="default-params"
             :id="`default-params-${reportIndex}`">
            <b-link v-b-toggle="`collapseSummary-${reportIndex}`"
                    href="#"
                    class="show-defaults pt-2 d-inline-block small">
                <span class="when-closed">Show</span>
                <span class="when-open">Hide</span> defaults...
            </b-link>
            <b-collapse :id="`collapseSummary-${reportIndex}`" class="collapseSummary">
                <p v-for="(param, paramIndex) in report.default_param_list"
                   class="default-params-collapse"
                   :id="`default-params-collapse-${reportIndex}-${paramIndex}`"
                   :key="paramIndex">{{ param.name }}: {{ param.value }}</p>
            </b-collapse>
        </div>
    </div>
    <p v-else class="no-params">
        No parameters
    </p>
</template>

<script lang="ts">
    import Vue from 'vue'
    import {WorkflowReportWithDependencies} from "../../utils/types";
    import {BLink} from "bootstrap-vue/esm/components/link";
    import {BCollapse} from "bootstrap-vue/esm/components/collapse";
    import {VBToggle} from 'bootstrap-vue';
    import {hasParams} from "../../utils/helpers.ts";

    interface Props {
        report: WorkflowReportWithDependencies;
        reportIndex: number;
    }

    interface Computed {
        hasParams: boolean
    }

    Vue.directive("b-toggle", VBToggle);

    export default Vue.extend<unknown, unknown, Computed, Props>({
        name: "RunWorkflowParameters",
        components: {
            BCollapse,
            BLink,
        },
        props: {
            report: {
                type: Object,
                required: true
            },
            reportIndex: {
                type: Number,
                required: true
            },
        },
        computed: {
            hasParams() {
                return hasParams(this.report)
            },
        },

    })
</script>