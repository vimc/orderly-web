<template>
    <div v-if="hasParams(report)">
        <p class="non-default-param"
            v-for="(param, paramIndex) in report.param_list"
            :key="paramIndex">{{ param.name }}: {{ param.value }}</p>
        <p v-if="!report.param_list.length">No non-default parameters</p>
        <div v-if="report.default_param_list.length > 0"
                :id="`default-params-${index}`">
            <b-link href="#"
                    class="show-defaults pt-2 d-inline-block small"
                    v-b-toggle="`collapseSummary-${index}`">
                <span class="when-closed">Show</span>
                <span class="when-open">Hide</span> defaults...
            </b-link>
            <b-collapse :id="`collapseSummary-${index}`">
                <p :id="`default-params-collapse-${index}-${paramIndex}`"
                    v-for="(param, paramIndex) in report.default_param_list"
                    :key="paramIndex">{{ param.name }}: {{ param.value }}</p>
            </b-collapse>
        </div>
    </div>
    <p v-else>No parameters</p>
</template>

<script lang="ts">
import Vue from 'vue'
import {WorkflowRunReportStatus, WorkflowReportWithDependencies} from "../../utils/types";
import {BLink} from "bootstrap-vue/esm/components/link";
import {BCollapse} from "bootstrap-vue/esm/components/collapse";
import {VBToggle} from 'bootstrap-vue';
import {hasParams} from "../../utils/helpers.ts";

interface Props {
    report: WorkflowRunReportStatus;
}

interface Methods {
    hasParams: (report: WorkflowReportWithDependencies) => boolean
}

Vue.directive("b-toggle", VBToggle);

export default Vue.extend<unknown, Methods, unknown, Props>({
    name: "runWorkflowParameters",
    components: {
        BCollapse,
        BLink,
    },
    props: {
        report: {
            type: Object,
            required: true
        },
    },
    methods: {
        hasParams(report) {
            return hasParams(report)
        },
    },
    
})
</script>