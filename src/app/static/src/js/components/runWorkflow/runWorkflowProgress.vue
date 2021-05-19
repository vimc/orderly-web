<template>
    <!-- <p>Run workflow progress is coming soon</p> -->
    <div class="containter mt-3" v-if="workflowRunSummaries">
        <div class="row mb-3">
            <!-- <h4 class="col">Worflow</h4>
            <table class="table-bordered col-10">
                <tr>
                    <td>Name</td>
                    <td>{{ workflowRunSummaries.status }}</td>
                    <td>Date</td>
                </tr>
            </table> -->
            <label for="workflows" class="form-label col">Workflow</label>
            <select name="workflows" id="workflows" class="form-select col-10" v-model="selectedWorkflowKey">
                <option v-for="workflow in workflowRunSummaries.data" :value="workflow.key">
                    <span >{{ workflow.name }}</span>
                    <span>Started: {{ formatDate(workflow.date) }}</span>
                    <!-- <div class="container">
                        <div class="col">{{ workflow.name }}</div>
                        <div class="col">{{ formatDate(workflow.date) }}</div>
                    </div> -->
                </option>
            </select>
        </div>
        <div class="row" v-if="workflowRunDetails">
            <label class="form-label col">Reports</label>
            <table class="table-bordered col-10">
                <!-- <tr v-for="report in workflowRunDetails.data">
                    <td>{{ report.name }}</td>
                    <td>{{ workflowRunDetails.status }}</td>
                    <td>{{ report.date }}</td>
                </tr> -->
                <tr>
                    <td>{{ workflowRunDetails.data.name }}</td>
                    <td>{{ workflowRunDetails.status }}</td>
                    <td>{{ formatDate(workflowRunDetails.data.date) }}</td>
                </tr>
            </table>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import { api } from "../../utils/api";
import {longTimestamp} from "../../utils/helpers";

export default Vue.extend({
    name: "runWorkflowProgress",
    data(){
        return {
            workflowRunSummaries: null,
            selectedWorkflowKey: null,
            workflowRunDetails: null
        }
    },
    methods: {
        getWorkflowRunSummaries() {
            // api.post('/v1/workflow/run/', {"reports":[{"name":"minimal"},{"name":"global"}]})
            //     .then(({data}) => {
            //         console.log(data)
            //         // this.reports = data.data;
            //         // this.error = "";
            //         // this.defaultMessage = "";
            //     })
            //     .catch((error) => {
            //         console.log(error)
            //         // this.error = error;
            //         // this.defaultMessage = "An error occurred fetching the running reports";
            //     });
            // this.reports = [];
            api.get('/workflows')
                .then(({data}) => {
                    console.log(data)
                    this.workflowRunSummaries = data
                    // this.reports = data.data;
                    // this.error = "";
                    // this.defaultMessage = "";
                })
                .catch((error) => {
                    console.log(error)
                    // this.error = error;
                    // this.defaultMessage = "An error occurred fetching the running reports";
                });
            // const status = {
            //     status: "success",
            //     errors: null,
            //     data: {
            //         workflow_key: "unspecialized_shrike",
            //         status: "success",
            //         reports: [
            //             {
            //                 key: "cthonophagous_nerka",
            //                 status: "success",
            //                 version: "20210513-101918-3b954953",
            //                 output: null,
            //                 queue: [],
            //             },
            //             {
            //                 key: "jeffersonite_gorilla",
            //                 status: "success",
            //                 version: "20210513-101919-6d9c0a92",
            //                 output: null,
            //                 queue: [],
            //             },
            //         ],
            //     },
            // };
            // this.workflowRunSummaries = status.data
            // console.log(status)
        },
        getWorkflowRunDetails(key){
            api.get(`/workflows/${key}`, )
                .then(({data}) => {
                    console.log("details", data)
                    this.workflowRunDetails = data
                    // this.reports = data.data;
                    // this.error = "";
                    // this.defaultMessage = "";
                })
                .catch((error) => {
                    console.log(error)
                    // this.error = error;
                    // this.defaultMessage = "An error occurred fetching the running reports";
                });
        },
        formatDate(date) {
            return longTimestamp(new Date(date));
        }
    },
    watch: {
        selectedWorkflowKey(){
            console.log(this.selectedWorkflowKey)
            this.getWorkflowRunDetails(this.selectedWorkflowKey)
        }
    },
    mounted() {
        this.getWorkflowRunSummaries();
    },
});
</script>