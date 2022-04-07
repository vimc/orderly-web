<template>
    <ul v-if="roles.length > 0" class="list-unstyled roles">
        <li v-for="role in roles"
            :id="role.name"
            :key="role.name"
            :class="['role', {'open':expanded[role.name]}, {'has-children': canAddMembers || (role.members.length > 0)}]">
            <div class="expander" @click="toggle(role.name)"></div>
            <span class="role-name" @click="toggle(role.name)" v-text="role.name"></span>

            <span v-if="canRemoveRole(role.name)"
                  class="remove d-inline-block ml-2 large"
                  @click="removed(role.name)">×</span>

            <user-list v-if="role.members.length > 0"
                       v-show="expanded[role.name]"
                       css-class="children"
                       :users="role.members"
                       :can-remove="canRemoveMembers"
                       @removed="function(email){removeMember(role.name, email)}"></user-list>

            <add-user-to-role v-if="canAddMembers && expanded[role.name]"
                              :role="role.name"
                              :available-users="availableUsersForRole(role)"
                              @added="$emit('added')"></add-user-to-role>

        </li>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </ul>
</template>

<script>
    import Vue from "vue";
    import UserList from "./userList.vue";
    import AddUserToRole from "../admin/addUserToRole";
    import ErrorInfo from "../errorInfo.vue";
    import {api} from "../../utils/api";

    export default {
        name: 'RoleList',
        components: {
            UserList,
            AddUserToRole,
            ErrorInfo
        },
        props: ["roles", "canRemoveRoles", "canRemoveMembers", "canAddMembers", "availableUsers", "permission"],
        data() {
            return {
                error: null,
                defaultMessage: "Something went wrong",
                expanded: {}
            }
        },
        methods: {
            toggle: function (roleName) {
                Vue.set(this.expanded, roleName, !this.expanded[roleName]);
            },
            availableUsersForRole: function(role) {
                return this.availableUsers.filter(u => role.members.map(m => m.email).indexOf(u) < 0)
            },
            canRemoveRole: function(role) {
                return this.canRemoveRoles && role !== "Admin"
            },
            removeMember: function (roleName, email) {
                api.delete(`/roles/${encodeURIComponent(roleName)}/users/${encodeURIComponent(email)}`)
                    .then(() => {
                        this.$emit('removedMember', roleName, email);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove ${email} from ${roleName}`;
                        this.error = error;
                    });
            },
            removed: function (roleName) {
                this.$emit("removed", roleName);
            }
        }
    };
</script>
