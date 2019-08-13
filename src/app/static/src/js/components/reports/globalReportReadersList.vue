<template>
    <div id="global-report-readers-list">
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <user-list :users="readers"
                   :can-remove="false"></user-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import ErrorInfo from "../errorInfo.vue";
    import UserList from "../permissions/userList.vue";
    import AddPermission from "../permissions/addPermission.vue";

    export default {
        name: 'globalReportReadersList',
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
        components: {
            UserList,
            ErrorInfo
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