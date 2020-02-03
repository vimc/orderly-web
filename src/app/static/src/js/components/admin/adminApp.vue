<template>
    <div class="row">
        <div class="col-4">
            <label class="font-weight-bold d-block">Manage roles</label>
            <manage-roles :roles="roles"
                          @changed="getAll"></manage-roles>
        </div>
        <div class="col-4 offset-2">
            <label class="font-weight-bold d-block">Manage permissions</label>
            <label class="font-weight-bold d-block">For roles</label>
            <manage-role-permissions
                    :roles="roles"
                    @changed="getAll"></manage-role-permissions>
            <hr/>
            <label class="font-weight-bold d-block">For individual users</label>
            <manage-user-permissions :all-users="users"></manage-user-permissions>
        </div>
    </div>
</template>

<script>
    import Vue from "vue";
    import manageRoles from "./manageRoles";
    import manageUserPermissions from "./manageUserPermissions";
    import {api} from "../../utils/api";
    import manageRolePermissions from "./manageRolePermissions";

    export default Vue.extend({
        components: {
            manageRoles: manageRoles,
            manageUserPermissions: manageUserPermissions,
            manageRolePermissions: manageRolePermissions
        },
        data() {
            return {
                users: [],
                roles: [],
                typeaheadEmails: []
            }
        },
        methods: {
            getAll: function () {
                this.getRoles();
                this.getUsers();
                this.getTypeaheadEmails();
            },
            getRoles: function () {
                api.get(`/roles/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            },
            getUsers: function () {
                api.get(`/users/`)
                    .then(({data}) => {
                        this.users = data.data
                    })
            }
        },
        mounted() {
            this.getAll()
        }
    })
</script>