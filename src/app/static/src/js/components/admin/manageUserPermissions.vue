<template>
    <div id="manage-users">
        <input v-model="searchStr"
               type="text"
               class="form-control"
               placeholder="type to search"/>
        <ul class="list-unstyled roles mt-2">
            <li v-for="u in filteredUsers"
                :key="u.email"
                :class="['role has-children', {'open':expanded[u.email]}]">
                <div class="expander" @click="toggle(u.email)"></div>
                <span class="role-name" @click="toggle(u.email)">{{ u.display_name }}</span>
                <div class="text-muted small email role-name">
                    {{ u.email }}
                </div>
                <permission-list v-show="expanded[u.email]"
                                 :permissions="u.direct_permissions.concat(u.role_permissions)"
                                 :user-group="u.email"
                                 :can-edit="true"
                                 @added="function(p) {addPermission(p, u)}"
                                 @removed="function(p) {removePermission(p, u)}"></permission-list>
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
        name: 'ManageUsers',
        components: {
            PermissionList,
            ErrorInfo
        },
        props: ["allUsers"],
        data() {
            return {
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
            toggle: function (email) {
                Vue.set(this.expanded, email, !this.expanded[email]);
            },
            removePermission: function (permission, user) {
                const scopeId = permission.scope_id;
                const scopePrefix = permission.scope_prefix;
                const query = (scopeId && scopePrefix) ? `?scopePrefix=${scopePrefix}&scopeId=${scopeId}` : "";

                api.delete(`/users/${encodeURIComponent(user.email)}/permissions/${permission.name}/${query}`)
                    .then(() => {
                        this.error = null;
                        this.$emit("changed");
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
                        this.$emit("changed");
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
        }
    };
</script>
