<template>
    <div id="report-readers-list">
        <label class="font-weight-bold">Report readers</label>
        <div>
            <div class="input-group mb-3">
                <input v-model="add_email" class="form-control form-control-sm" type="text" placeholder="user email" value />
                <div class="input-group-append">
                    <button v-on:click="add" type="submit" class="btn btn-sm">Add reader</button>
                </div>
            </div>
            <ul class="list-unstyled report-readers">
                <li v-for="reader in readers">
                    <span>{{reader.display_name}}</span>
                    <div class="text-muted small" style="margin-top: -1rem">{{reader.username}}</div>
                </li>
            </ul>
        </div>
        <div class="text-danger mt-3" v-if="error.length > 0">
            {{error}}
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";

    export default {
        name: 'reportReadersList',
        props: ['report', 'initial_readers'],
        data() {
            return {
                add_email: "",
                error: "",
                readers: []
            }
        },
        mounted() {
            this.refreshReaders();
        },
        methods: {
            add: function () {
                const data = {
                    name: "reports.read",
                    action: "add",
                    scope_prefix: "report",
                    scope_id: this.report.name
                };

                api.post(`/users/${encodeURIComponent(this.add_email)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.refreshReaders();
                        this.add_email = "";
                        this.error = "";
                    })
                    .catch(() => {
                        this.error = "Error: could not add reader";
                    });
            },
            refreshReaders: function() {
                api.get(`/users/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.readers = data.data
                })
                    .catch(() => {
                    this.error = "Error: could not fetch list of readers";
                })
            }
        }
    };
</script>