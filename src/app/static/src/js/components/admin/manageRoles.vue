<template>
    <div id="role-list">
        <role-list :can-remove-members="true"
                   :can-remove-roles="false"
                   :roles="roles"></role-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import UserList from "../permissions/userList.vue";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'manageRoles',
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
                api.get(`/roles/`)
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