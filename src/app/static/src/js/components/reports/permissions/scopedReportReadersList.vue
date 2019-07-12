<template>
    <div id="report-readers-list">
        <add-permissions :report="report"
                         placeholder="email"
                         add-text="Add user"
                         :current-items="readers"
                         :available-items="availableUsers"
                         @added="getReaders"></add-permissions>
        <error-info :default-message="defaultMessage" :error="error"></error-info>
        <reader-list :readers="readers" :can-remove="true" @remove="remove"></reader-list>
    </div>
</template>

<script>
    import {api, userService} from "../../../utils/api";
    import ReaderList from "./readerList.vue";
    import AddPermissions from "./addPermissions.vue";
    import ErrorInfo from "../../errorInfo.vue";

    export default {
        name: 'reportReadersList',
        components: {ErrorInfo, ReaderList, AddPermissions},
        props: ['report', 'initialReaders'],
        data() {
            return {
                readers: [],
                allUsers: [],
                error: null,
                defaultMessage: "Something went wrong"
            }
        },
        mounted() {
            this.getReaders();
            this.getUserEmails();
        },
        computed: {
            availableUsers: function () {
                return this.allUsers.filter(x =>
                    !(new Set(this.readers.map(r => r.email))).has(x));
            }
        },
        methods: {
            remove: function (email) {
                userService.removeUserGroup(email, this.report.name)
                    .then(() => {
                        this.getReaders();
                    })
                    .catch((error) => {
                        this.defaultMessage = `Could not remove ${email}`;
                        this.error = error
                    });
            },
            getUserEmails: function () {
                api.get(`/users/`)
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
                        this.defaultMessage = "Could not fetch users";
                        this.error = error
                    })
            }
        }
    };
</script>