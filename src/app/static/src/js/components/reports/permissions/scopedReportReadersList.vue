<template>
    <div id="report-readers-list">
        <manage-permissions :report="report"
                            placeholder="email"
                            add-text="Add user"
                            :current-items="readers"
                            :available-items="availableUsers"
                            @added="getReaders"></manage-permissions>
        <error-info default-message="Could not remove user" :error="removeError"></error-info>
        <reader-list :readers="readers" :can-remove="true" @remove="remove"></reader-list>
        <error-info default-message="Could not fetch list of users" :error="getError"></error-info>
    </div>
</template>

<script>
    import {api, userService} from "../../../utils/api";
    import ReaderList from "./readerList.vue";
    import ManagePermissions from "./managePermissions.vue";
    import ErrorInfo from "../../errorInfo.vue";

    export default {
        name: 'reportReadersList',
        components: {ErrorInfo, ReaderList, ManagePermissions},
        props: ['report', 'initialReaders'],
        data() {
            return {
                readers: [],
                allUsers: [],
                removeError: null,
                getError: null
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
                        this.refreshReaders();
                    })
                    .catch((error) => {
                        this.removeError = error
                    });
            },
            getUserEmails: function () {
                api.get(`/users/`)
                    .then(({data}) => {
                        this.all_users = data.data
                    })
            },
            getReaders: function () {
                api.get(`/users/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.readers = data.data
                    })
                    .catch((error) => {
                        this.getError = error
                    })
            }
        }
    };
</script>