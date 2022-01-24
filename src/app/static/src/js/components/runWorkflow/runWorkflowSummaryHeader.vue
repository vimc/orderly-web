<template>
<section>
    <h2 id="summary-header">Summary</h2>
    <div id="summary-warning" class="row mt-3" v-if="hasMissingDependencies">
        <div class="col-auto">
            <alert-triangle-icon size="2x" stroke="red" class="custom-class"/>
        </div>
        <div class="col-auto">
            <span class="d-inline-block pb-2">Some reports depend on the latest version of other reports that are not included in your workflow:</span>
            <div class="missingDependency" v-for="(missing_dependencies, report) in workflowSummary.missing_dependencies" :key="report">
                <span v-if="missing_dependencies.length" class="font-weight-bold"> {{ report }}</span>
                <ul v-for="missing_dependency in missing_dependencies" class="styled"  :key="missing_dependency">
                    <li>{{ missing_dependency }}</li>
                </ul>
            </div>
        </div>
    </div>
</section>
</template>

<script lang="ts">
import Vue from 'vue'
import {WorkflowSummaryEndpoint} from "../../utils/types";
import {AlertTriangleIcon} from "vue-feather-icons"

interface Props {
    workflowSummary: WorkflowSummaryEndpoint | null
}

interface Computed {
    hasMissingDependencies: boolean;
}
export default Vue.extend<unknown, Computed, unknown, Props>({
    name: "runWorkflowSummaryHeader",
        components: {
            AlertTriangleIcon
        },
    props: {
        workflowSummary: {
            type: Object || null,
            default: null
        }
    },
    computed: {
        hasMissingDependencies() {
            return this.workflowSummary && Object.keys(this.workflowSummary.missing_dependencies).length;
        },
    }
    
})
</script>