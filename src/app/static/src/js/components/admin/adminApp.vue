<template>
    <div class="row">
        <div class="col-4">
            <label class="font-weight-bold d-block">Manage roles</label>
            <manage-roles :roles="roles"
                          @changed="getAll"></manage-roles>
        </div>
        <div class="col-4">
            <label class="font-weight-bold d-block">Manage permissions</label>
            <label class="font-weight-bold d-block">For roles</label>
            <manage-role-permissions :roles="roles"
                                     @changed="getAll"></manage-role-permissions>
            <hr/>
            <label class="font-weight-bold d-block">For individual users</label>
            <manage-user-permissions :all-users="users" @changed="getUsers"></manage-user-permissions>
        </div>
        <div class="col-4">
            <div v-if="showSettings">
                <label class="font-weight-bold">Settings</label>
                <settings></settings>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import manageRoles from "./manageRoles.vue";
    import manageUserPermissions from "./manageUserPermissions.vue";
    import {api} from "../../utils/api";
    import manageRolePermissions from "./manageRolePermissions.vue";
    import settings from "./settings.vue";

    declare const canAllowGuest: boolean;

    export default Vue.extend({
        components: {
            manageRoles: manageRoles,
            manageUserPermissions: manageUserPermissions,
            manageRolePermissions: manageRolePermissions,
            settings: settings
        },
        data() {
            return {
                users: [],
                roles: []
            }
        },
        computed: {
            showSettings() {
                return canAllowGuest;
            }
        },
        mounted() {
            this.getAll()
        },
        methods: {
            getAll: function () {
                this.getRoles();
                this.getUsers();
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
        }
    })
</script>