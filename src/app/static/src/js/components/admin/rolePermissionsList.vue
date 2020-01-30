<template>
    <ul class="list-unstyled roles" v-if="roles.length > 0">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}, {'has-children': role.permissions.length > 0}]">
            <div class="expander" v-on:click="toggle(index)"></div>
            <span v-text="role.name" v-on:click="toggle(index)" class="role-name"></span>
            <permission-list v-if="role.permissions.length > 0"
                             v-show="expanded[index]"
                             cssClass="children"
                             :permissions="role.permissions"
                             :user-group="role.name"
                             @added="function(p) {addPermission(p, role.name)}"
                             @removed="function(p) {removePermission(p, role.name)}"></permission-list>

            <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import PermissionList from "./permissionList.vue";
    import ErrorInfo from "../errorInfo.vue";
    import {api} from "../../utils/api";

    export default {
        name: 'roleList',
        props: ["roles"],
        data() {
            return {
                error: null,
                defaultMessage: "Something went wrong",
                expanded: {}
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            },
            removePermission: function (permission, roleName) {
                const queryString = permission.scope_prefix ?
                    `?scopePrefix=${encodeURIComponent(permission.scope_prefix)}&scopeId=${encodeURIComponent(permission.scope_id)}` : "";

                api.delete(`/roles/${encodeURIComponent(roleName)}/permissions/${permission.name}/${queryString}`)
                    .then(() => {
                        this.$emit('removed', roleName, permission);
                        this.error = null;
                        this.defaultMessage = "Something went wrong";
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove permission from ${roleName}`;
                        this.error = error;
                    });
            },
            addPermission: function (permission, roleName) {
                const data = {
                    name: permission,
                    scope_id: "",
                    scope_prefix: null
                };

                api.post(`/roles/${encodeURIComponent(roleName)}/permissions/`, data)
                    .then(() => {
                        this.error = null;
                        user.direct_permissions.push(data);
                        user.direct_permissions.sort()
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add ${permission} to ${roleName}`;
                    });
            },
        },
        components: {
            PermissionList,
            ErrorInfo
        }
    };
</script>
