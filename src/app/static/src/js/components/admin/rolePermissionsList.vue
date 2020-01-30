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
                //NB This route is about to change! Merge with master after mrc-1294 has been merged
                const data = {
                    ...permission,
                    action: "remove"
                };
                api.post(`/user-groups/${encodeURIComponent(roleName)}/actions/associate-permission`, data)
                    .then(() => {
                        this.$emit('removed', roleName, permission);
                        this.error = null;
                        this.defaultMessage = "Something went wrong";
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove permission from ${roleName}`;
                        this.error = error;
                    });
            }
        },
        components: {
            PermissionList,
            ErrorInfo
        }
    };
</script>
