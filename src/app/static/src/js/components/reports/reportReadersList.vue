<template>
    <div id="report-readers-list">
        <label class="font-weight-bold">Specific read access</label>
        <div>
            <div class="mb-3">
                <vue-bootstrap-typeahead
                        size="sm"
                        v-model="newUser"
                        placeholder="email"
                        :data="availableUsers">
                    <template slot="append">
                        <button v-on:click="add" type="submit" class="btn btn-sm">Add user</button>
                    </template>
                </vue-bootstrap-typeahead>
                <div class="text-danger small" v-if="error.length > 0">
                    {{error}}
                </div>
            </div>
            <ul class="list-unstyled report-readers">
                <li v-for="reader in readers" v-bind:id="reader.email">
                    <span class="reader-display-name">{{reader.display_name}}</span>
                    <span v-on:click="remove(reader.email)" class="remove-reader d-inline-block ml-2 large">×</span>
                    <div class="text-muted small email">{{reader.email}}</div>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'

    export default {
        name: 'reportReadersList',
        props: ['report'],
        data() {
            return {
                newUser: "",
                error: "",
                readers: [],
                allUsers: []
            }
        },
        mounted() {
            this.getReaders();
            this.getUserEmails();
        },
        components: {
            VueBootstrapTypeahead
        },
        computed: {
            availableUsers: function () {
                return this.allUsers.filter(x =>
                    !(new Set(this.readers.map(r => r.email))).has(x));
            }
        },
        watch: {
            newUser() {
                this.error = ""
            }
        },
        methods: {
            add: function () {
                this.postAssociatePermissionAction("add", this.newUser);
            },
            remove: function (email) {
                this.postAssociatePermissionAction("remove", email);
            },
            getUserEmails: function () {
                api.get(`/emails/`)
                    .then(({data}) => {
                        this.allUsers = data.data
                    })
            },
            getReaders: function () {
                api.get(`/users/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.readers = data.data
                    })
                    .catch((error) => {
                        this.handleError(error, "could not fetch list of users");
                    })
            },
            handleError: function (error, defaultMessage) {
                this.error = "Error: " + (api.errorMessage(error.response) || defaultMessage);
            },
            postAssociatePermissionAction: function (action, user) {

                if (action === "add" && !new Set(this.availableUsers).has(user)) {
                    this.error = "You must enter a valid user email";
                    return;
                }

                const data = {
                    name: "reports.read",
                    action: action,
                    scope_prefix: "report",
                    scope_id: this.report.name
                };

                api.post(`/user-groups/${encodeURIComponent(user)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.getReaders();
                        this.newUser = "";
                        this.error = "";
                    })
                    .catch((error) => {
                        this.handleError(error, `could not ${action} user`);
                    });
            }
        }
    };
</script>