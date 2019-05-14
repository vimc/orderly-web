<template>
    <div id="run-report" class="mt-5">
        <label class="font-weight-bold">Run</label>
        <div>
            <div>Run this report to create a new version.</div>
            <div v-bind:class="['modal-background', {'modal-hide':!showModal}, {'modal-show':showModal}]">
                <div class="modal-main px-3 py-3">
                    <div class="mb-2 font-weight-bold">Confirm run report</div>
                    <div class="mb-2">Are you sure you want to run this report?</div>
                    <div class="modal-buttons">
                        <button v-on:click="run" id="confirm-run-btn" class="btn submit mr-3">Yes</button>
                        <button v-on:click="cancelRun" id="cancel-run-btn" class="btn btn-default">No</button>
                    </div>
                </div>
            </div>
        </div>
        <button v-on:click="confirmRun" class="btn mt-2" type="submit">Run report</button>
    </div>
</template>

<script>
    import axios from "axios";
     export default {
        name: 'runReport',
        props: ['report'],
        data() {
            return {
                error: "",
                showModal: false
            }
        },
        methods: {
            confirmRun: function() {
                this.showModal = true;
            },
            cancelRun: function() {
                this.showModal = false;
            },
            run: function () {
                this.showModal = false;
                axios.post(`/reports/${this.report.name}/run/`,
                    null,
                    {withCredentials: true})
                    .then(() => {
                        this.$emit('running');
                        this.error = "";
                    })
                    .catch(() => {
                        this.error = "Could not run report";
                    });
            }
        }
    };
</script>