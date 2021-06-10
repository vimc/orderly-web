<template>
    <div>
        <h2 id="add-report-header" class="pb-2">Add reports</h2>
        <!--<div class="pb-4">
            <h2 id="git-header">Git</h2>
            <div class="form-group row">
                <label class="col-sm-2 col-form-label text-right"></label>
                <div>
                    <div id="workflow-branch-div" class="form-group row">
                        <label class="col-sm-3 col-form-label text-right">Branch</label>
                        <div class="col-sm-8">
                            <select class="form-control" id="workflow-git-branch">
                                <option value="master">master</option>
                            </select>
                        </div>
                    </div>
                    <div id="workflow-commit-div" class="form-group row">
                        <label class="col-sm-3 col-form-label text-right">Commit</label>
                        <div class="col-sm-8">
                            <select class="form-control" id="workflow-git-commit">
                                <option value="commit ">adfbd130 (2021-03-8 11:36:19)</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="pb-4">
            <h2 id="report-sub-header">Reports</h2>
            <div>
                <div id="preprocess-div" class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">Preprocess</label>
                    <div>
                    <div class="form-group row">
                        <label for="n-min" class="col-sm-4 col-form-label text-right">nmin:</label>
                        <div class="col-sm-8 input-group">
                            <input type="text" class="form-control mr-2" id="n-min" placeholder="7">
                            <button id="workflow-remove-button" type="button" class="px-2 btn btn-primary">Remove report</button>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label for="n-max" class="col-sm-4 col-form-label text-right">nmax:</label>
                        <div class="col-sm-4">
                            <input type="text" class="form-control" id="n-max" placeholder="16">
                        </div>
                    </div>
                    </div>
                </div>

                <div id="add-report-div" class="form-group row">
                    <label for="workflow-report" class="col-sm-2 col-form-label text-right">Add report</label>
                    <div class="col-sm-4 input-group">
                        <input type="text" @input="validateStep" class="form-control mr-2" id="workflow-report" placeholder="16">
                        <button id="add-report-button" type="button" class="px-2 btn btn-primary">Add report</button>
                    </div>
                </div>
            </div>
        </div>-->
    </div>
</template>

<script lang="ts">
import Vue from "vue"
import {RunWorkflowMetadata} from "../../utils/types";

interface Props {
    workflowMetadata: RunWorkflowMetadata | null
}

interface Computed {
    validateStep: void
}

export default Vue.extend<unknown, Computed, unknown, Props>({
    name: "runWorkflowReport",
    props: {
        workflowMetadata: null
    },
    computed: {
        validateStep: function () {
            /**
             *  Valid step should be set to true or false once validation is complete
             */
            this.$emit("valid", true)
        }
    }
})
</script>
