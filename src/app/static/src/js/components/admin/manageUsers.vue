<template>
    <div id="user-list">
        <input type="text"
               class="form-control"
               v-model="searchStr"
               placeholder="type to search"/>
        <ul class="list-unstyled roles mt-2">
            <li v-for="(u, index) in filteredUsers"
                v-bind:class="['role', {'open':expanded[index]}, {'has-children': u.direct_permissions.length > 0}]">
                <div class="expander" v-on:click="toggle(index)"></div>
                <span v-on:click="toggle(index)" class="role-name">{{u.display_name}}</span>
                <div class="text-muted small email role-name">{{u.email}}</div>

                <permission-list v-show="expanded[index]"
                                 :permissions="u.direct_permissions"
                                 :email="u.email"
                                 @added="function(p) {addPermission(p, u)}"
                                 @removed="function(p) {removePermission(p, u)}"
                                 :all-permissions="allPermissions"></permission-list>
            </li>
        </ul>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import Vue from "vue";
    import PermissionList from "./permissionList.vue";
    import ErrorInfo from "../errorInfo.vue";

    export default {
        name: 'manageUsers',
        mounted() {
            this.getUsers();
            this.getPermissions();
        },
        data() {
            return {
                allUsers: [],
                allPermissions: [],
                searchStr: "",
                expanded: {},
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            filteredUsers: function () {
                return this.allUsers.filter(u => this.userMatches(u, this.searchStr))
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            },
            getUsers: function () {
                api.get(`/users/`)
                    .then(({data}) => {
                        this.allUsers = data.data
                    })
            },
            getPermissions: function () {
                api.get(`/typeahead/permissions/`)
                    .then(({data}) => {
                        this.allPermissions = data.data
                    })
            },
            removePermission: function (permission, user) {
                const scopeId = permission.scope_id;
                const scopePrefix = permission.scope_prefix;
                const query = (scopeId && scopePrefix) ? `?scopePrefix=${scopePrefix}&scopeId=${scopeId}` : "";

                api.delete(`/users/${encodeURIComponent(user.email)}/permissions/${permission.name}/${query}`)
                    .then(() => {
                        this.error = null;
                        user.direct_permissions.splice(user.direct_permissions.indexOf(permission), 1);
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not remove ${permission.name} from ${user.email}`;
                    });
            },
            addPermission: function (permission, user) {
                const data = {
                    name: permission,
                    scope_id: "",
                    scope_prefix: null
                };

                api.post(`/users/${encodeURIComponent(user.email)}/permissions/`, data)
                    .then(() => {
                        this.error = null;
                        user.direct_permissions.push(data);
                        user.direct_permissions.sort()
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add ${permission} to ${user.email}`;
                    });
            },
            userMatches: function (u, searchStr) {
                return searchStr.length > 1 && (this.stringMatches(u.display_name, searchStr) ||
                    this.stringMatches(u.email, searchStr) || this.stringMatches(u.username, searchStr))
            },
            stringMatches: function (a, b) {
                return a.toLowerCase().indexOf(b.toLowerCase()) > -1
            }
        },
        components: {
            PermissionList,
            ErrorInfo
        }
    };
</script>
