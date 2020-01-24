<template>
    <div id="report-readers-global-list">
        <role-list :can-remove-members="false"
                       :can-remove-roles="false"
                       :show-members="true"
                       :roles="roles"></role-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import UserList from "../permissions/userList.vue";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'globalReaderRolesList',
        mounted() {
            this.getRoles();

        },
        data() {
            return {
                roles: []
            }
        },
        methods: {
            getRoles: function () {
                api.get(`/roles/report-readers/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            }
        },
        components: {
            RoleList,
            UserList
        }
    };
</script>