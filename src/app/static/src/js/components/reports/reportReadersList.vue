<template>
    <div id="scoped-report-readers-list">
        <add-report-reader :report-name="report.name"
                           type="user"
                           :available-user-groups="availableUsers"
                           @added="getReaders"></add-report-reader>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <user-list :users="readers"
                   :can-remove="true"
                   @removed="removeUser"></user-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import UserList from "../permissions/userList.vue";
    import AddReportReader from "../permissions/addReportReader";

    export default {
        name: 'ReportReadersList',
        components: {
            AddReportReader,
            UserList,
            ErrorInfo
        },
        props: ['report'],
        data() {
            return {
                error: "",
                defaultMessage: "",
                readers: [],
                allUsers: []
            }
        },
        computed: {
            availableUsers: function () {
                return this.allUsers.filter(x =>
                    !(new Set(this.readers.map(r => r.email))).has(x));
            },
            permission: function () {
                return {
                    name: "reports.read",
                    scope_prefix: "report",
                    scope_id: this.report.name
                }
            }
        },
        mounted() {
            this.getReaders();
            this.getUserEmails();
        },
        methods: {
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
            removeUser: function (email) {
                const scopeId = this.permission.scope_id;
                const scopePrefix = this.permission.scope_prefix;
                const query = (scopeId && scopePrefix) ? `?scopePrefix=${scopePrefix}&scopeId=${scopeId}` : "";

                api.delete(`/users/${encodeURIComponent(email)}/permissions/${this.permission.name}/${query}`)

                    .then(() => {
                        this.getReaders();
                        this.error = null;
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not remove ${email}`;
                    });
            }
        }
    };
</script>