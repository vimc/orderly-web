<template>
<div>
    <h2>Summary</h2>
    <div>
        <ul>
            <li>Preprocess</li>
            <li>Process</li>
            <li>Postprocess</li>
        </ul>
    </div>
    <p>{{ "Summary Page:" + workflowMetadata.placeholder}}</p>
</div>
</template>

<script lang="ts">
import Vue from "vue"
import {RunWorkflowMetadata} from "../../utils/types";

interface Props {
    workflowMetadata: RunWorkflowMetadata
}

export default Vue.extend<unknown, unknown, unknown, Props>({
    name: "runWorkflowSummary",
    props: {
        workflowMetadata: null
    }
})
</script>
