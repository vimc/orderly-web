<template>
    <div id="scoped-report-readers-list">
        <add-permission :permission="permission"
                        type="user"
                        :available-user-groups="availableUsers"
                        @added="getReaders"></add-permission>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <user-list :users="readers" :can-remove="true" @removed="getReaders"></user-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import ErrorInfo from "../errorInfo.vue";
    import UserList from "../permissions/userList.vue";
    import AddPermission from "../permissions/addPermission.vue";

    export default {
        name: 'reportReadersList',
        props: ['report'],
        data() {
            return {
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
            AddPermission,
            UserList,
            ErrorInfo,
            VueBootstrapTypeahead
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
                };
            }
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
            }
        }
    };
</script>