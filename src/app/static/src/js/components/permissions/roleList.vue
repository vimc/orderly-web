<template>
    <ul class="list-unstyled roles" v-if="roles.length > 0">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}, {'has-members': role.members.length > 0}]">
            <div class="expander" v-on:click="toggle(index)"></div>
            <span v-text="role.name" v-on:click="toggle(index)" class="role-name"></span>

            <span v-if="canRemoveRoles" v-on:click="function(){removeRole(role.name)}"
                  class="remove-user-group d-inline-block ml-2 large">×</span>

            <user-list v-if="role.members.length > 0"
                       v-show="expanded[index]"
                       cssClass="members"
                       :users="role.members"
                       :canRemove="canRemoveMembers"
                       @removed="function(email){removeMember(role.name, email)}"></user-list>

            <error-info :default-message="defaultMessage" :api-error="error"></error-info>
            <add-user-to-role v-if="canAddMembers && expanded[index]"
                        :role="role.name"
                        :available-users="availableUsersForRole(role)"
                        @added="$emit('added')"></add-user-to-role>

        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import UserList from "./userList.vue";
    import AddUserToRole from "../admin/addUserToRole";
    import ErrorInfo from "../errorInfo.vue";
    import {api} from "../../utils/api";

    export default {
        name: 'roleList',
        props: ["roles", "canRemoveRoles", "canRemoveMembers", "canAddMembers", "availableUsers", "permission"],
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
            availableUsersForRole: function(role) {
                return this.availableUsers.filter(u => role.members.map(m => m.email).indexOf(u) < 0)
            },
            removeMember: function (roleName, email) {
                api.delete(`/roles/${encodeURIComponent(roleName)}/users/${encodeURIComponent(email)}`)
                    .then(() => {
                        this.$emit('removed', roleName, email);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove ${email} from ${roleName}`;
                        this.error = error;
                    });
            },
            removeRole: function (roleName) {
                const scopeId = this.permission.scope_id;
                const scopePrefix = this.permission.scope_prefix;
                const query = (scopeId && scopePrefix) ? `?scopePrefix=${scopePrefix}&scopeId=${scopeId}` : "";

                api.delete(`/roles/${encodeURIComponent(roleName)}/permissions/${this.permission.name}/${query}`)
                    .then(() => {
                        this.$emit("removed", roleName);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove ${roleName}`;
                        this.error = error;
                    });
            }
        },
        components: {
            UserList,
            AddUserToRole,
            ErrorInfo
        }
    };
</script>
