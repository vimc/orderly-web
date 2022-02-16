<template>
    <div id="manage-role-permissions">
        <ul v-if="roles.length > 0" class="list-unstyled roles">
            <li v-for="(role, index) in roles"
                :id="role.name"
                :key="role.name"
                :class="['role', 'has-children', {'open':expanded[index]}]">
                <div class="expander" @click="toggle(index)"></div>
                <span class="role-name" @click="toggle(index)" v-text="role.name"></span>
                <permission-list v-show="expanded[index]"
                                 css-class="children"
                                 :permissions="role.permissions"
                                 :user-group="role.name"
                                 :can-edit="role.name !== 'Admin'"
                                 @added="function(p) {addPermission(p, role)}"
                                 @removed="function(p) {removePermission(p, role.name)}"></permission-list>
            </li>
        </ul>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import Vue from "vue";
    import PermissionList from "./permissionList.vue";
    import ErrorInfo from "../errorInfo.vue";
    import {api} from "../../utils/api";

    export default {
        name: 'RoleList',
        components: {
            PermissionList,
            ErrorInfo
        },
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
                        this.$emit('changed', roleName, permission);
                        this.error = null;
                        this.defaultMessage = "Something went wrong";
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove permission from ${roleName}`;
                        this.error = error;
                    });
            },
            addPermission: function (permission, role) {
                const data = {
                    name: permission,
                    scope_id: "",
                    scope_prefix: null
                };

                api.post(`/roles/${encodeURIComponent(role.name)}/permissions/`, data)
                    .then(() => {
                        this.error = null;
                        this.$emit('changed');
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add ${permission} to ${role.name}`;
                    });
            },
        }
    };
</script>
