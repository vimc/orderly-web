<template>
<div>
    <div v-if="showReports" id="report-form-group" class="form-group row">
        <label for="report" class="col-sm-2 col-form-label text-right">Show logs for</label>
        <div class="col-sm-6">
            <report-list id="report" 
            :reports="reports" 
            :report.sync="selectedReport" 
            :key.sync="selectedReportKey"/>
        </div>
        <button @click="getAllReports()">Refresh</button>
    </div>
    <div v-else>No reports have been ran yet</div>
</div>
</template>

<script lang="ts">
    import Vue from "vue"
    import ReportList from "../runReport/reportList.vue";
    import {api} from "../../utils/api";
    import EventBus from './../../eventBus';

    interface Computed {
        showReports: boolean
    }

    interface Methods {
        getAllReports: () => void
    }

    interface Data {
        reports: [],
        selectedReportKey: string,
        selectedReport: string,
        error: string,
        defaultMessage: string,
        reportId: string
    }

    export default Vue.extend<Data, Methods, Computed, unknown>({
        name: "reportLog",
        components: {
            ReportList
        },
        data(): Data {
            return {
                reports: [],
                selectedReportKey: "",
                reportId: "",
                selectedReport: "",
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            showReports: function () {
                return this.reports.length > 0
            }
        },
        methods: {
            getAllReports() {
                this.reports = [];
                api.get('/running/')
                    .then(({data}) => {
                        this.reports = data.data;
                        this.error = "";
                        this.defaultMessage = "";
                        console.log('get all reports fired', data.data)
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = "An error occurred fetching the running reports";
                    });
            }
        },
        mounted(){
            // setInterval(this.getAllReports(), 3000);
            this.getAllReports();
            EventBus.$on("ranReport", function (payload) {
                console.log('emitted received in reportLog')
                this.getAllReports()
            });
        },
        watch: {
            selectedReportKey() {
                console.log(this.selectedReportKey)
            }
        }
    })
</script>
