<template>
    <!-- <p>Run workflow progress is coming soon</p> -->
    <div class="containter">
        <div class="row">
            <h4 class="col">Worflow</h4>
            <table class="table col-10">
                <tr>
                    <td>Name</td>
                    <td>{{ workflowStatus.status }}</td>
                    <td>Date</td>
                </tr>
            </table>
        </div>
        <div class="row">
            <h4 class="col">Reports</h4>
            <table class="table col-10">
                <tr v-for="report in workflowStatus.reports">
                    <td>Name</td>
                    <td>{{ report.status }}</td>
                    <td>Date</td>
                </tr>
            </table>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import { api } from "../../utils/api";

export default Vue.extend({
    name: "runWorkflowProgress",
    data(){
        return {
            workflowStatus: {}
        }
    },
    methods: {
        getWorkflowStatus() {
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
            // api.get('/v1/workflow/unspecialized_shrike/status/')
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
            const status = {
                status: "success",
                errors: null,
                data: {
                    workflow_key: "unspecialized_shrike",
                    status: "success",
                    reports: [
                        {
                            key: "cthonophagous_nerka",
                            status: "success",
                            version: "20210513-101918-3b954953",
                            output: null,
                            queue: [],
                        },
                        {
                            key: "jeffersonite_gorilla",
                            status: "success",
                            version: "20210513-101919-6d9c0a92",
                            output: null,
                            queue: [],
                        },
                    ],
                },
            };
            this.workflowStatus = status.data
            // console.log(status)
        },
    },
    mounted() {
        this.getWorkflowStatus();
    },
});
</script>