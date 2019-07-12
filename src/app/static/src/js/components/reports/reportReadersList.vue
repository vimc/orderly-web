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
                <error-info :default-message="defaultMessage" :error="error"></error-info>
            </div>
            <user-list :users="readers" :can-remove="true" @remove="remove"></user-list>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import ErrorInfo from "../errorInfo.vue";
    import UserList from "../permissions/userList.vue";

    export default {
        name: 'reportReadersList',
        props: ['report'],
        data() {
            return {
                newUser: "",
                error: "",
                defaultMessage: "",
                readers: [],
                allUsers: []
            }
        },
        mounted() {
            this.getReaders();
            this.getUserEmails();
        },
        components: {
            UserList,
            ErrorInfo,
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
                api.get(`/typeahead/emails/`)
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
                        this.error = error;
                        this.defaultMessage = "could not fetch list of users";
                    })
            },
            postAssociatePermissionAction: function (action, user) {

                if (action === "add" && !new Set(this.availableUsers).has(user)) {
                    this.error = "you must enter a valid user email";
                    this.defaultMessage = "you must enter a valid user email";
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
                        this.error = null;
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not ${action} user`;
                    });
            }
        }
    };
</script>