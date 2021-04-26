<template>
    <div>
        <h2 id="run-header">Run workflow</h2>
        <div id="workflow-name-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Name</label>
            <div class="col-sm-4">
                <input type="text" class="form-control" id="run-workflow-name" placeholder="Impact estimates">
            </div>
        </div>
        <div id="workflow-source-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Database "source"</label>
            <div class="col-sm-4">
                <input type="text" class="form-control" id="run-workflow-database_source" placeholder="Production">
            </div>
        </div>
        <div id="workflow-changelog-div" class="form-group row">
            <label id="workflow-changelog-label" class="col-sm-4 col-form-label text-left">Changelog message</label>
            <div class="col-sm-4">
                <select class="form-control" id="run-workflow-changelog" >
                    <option value="interim update">
                        Interim update
                    </option>
                </select>
            </div>
        </div>
        <div id="workflow-changelog-type-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Changelog type</label>
            <div class="col-sm-4">
                <input type="text" class="form-control" id="run-workflow-changelog-type" placeholder="Internal">
            </div>
        </div>
        <div id="workflow-tags-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Report version tags</label>
            <div class="col-sm-4">
                <input type="text" class="form-control" id="run-workflow-report-version-tags" placeholder="interim estimates">
            </div>
        </div>
        <div id="workflow-completion-div" class="form-group row">
            <label class="col-sm-4 col-form-label text-left">Only commit reports on workflow completion</label>
            <div id="run-workflow-ticked" class="col-sm-4">
                <p>ticked</p>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import {RunWorkflowMetadata} from "../../utils/types";

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
}

export default Vue.extend<unknown, unknown, unknown, Props>({
    name: "runWorkflowRun",
    props: {
        workflowMetadata: null
    }
})
</script>