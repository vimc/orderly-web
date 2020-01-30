<template>
    <div id="role-list">
        <role-permissions-list
                   :roles="roles"
                   @removed="getRoles"></role-permissions-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RolePermissionsList from "./rolePermissionsList.vue";

    export default {
        name: 'manageRolePermissions',
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
            RolePermissionsList
        }
    };
</script>