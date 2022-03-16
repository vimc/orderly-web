<template>
    <div id="global-report-readers-list">
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <user-list :users="readers"
                   :can-remove="false"></user-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import UserList from "../permissions/userList.vue";

    export default {
        name: 'GlobalReportReadersList',
        components: {
            UserList,
            ErrorInfo
        },
        props: [],
        data() {
            return {
                error: "",
                defaultMessage: "",
                readers: []
            }
        },
        mounted() {
            this.getReaders();
        },
        methods: {
            getReaders: function () {
                api.get(`/users/report-readers/`)
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