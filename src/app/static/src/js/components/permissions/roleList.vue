<template>
    <ul class="list-unstyled roles" v-if="roles.length > 0">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}, {'has-members': role.members.length > 0}]">
            <div class="expander" v-on:click="toggle(index)"></div>
            <span v-text="role.name" class="role-name" v-on:click="toggle(index)"></span>

            <remove-permission v-if="canRemoveRoles"
                               :user-group="role.name"
                               :permission="permission"
                               @removed="$emit('removed', 'role')"></remove-permission>

            <user-list v-if="role.members.length > 0 && showMembers"
                       v-on:click="function(e){e.stopPropagation()}"
                       v-show="expanded[index]"
                       cssClass="members"
                       :users="role.members"
                       :permission="permission"
                       :role="permission ? '' : role.name"
                       :canRemove="canRemoveMembers"
                       @removed="function(e){removed(e,role)}"></user-list>
            <add-user-to-role v-if="canAddMembers && expanded[index]"
                        :role="role.name"
                        :available-users="availableUsersForRole(role)"
                        @added="function(e){addedUserToRole(e,role)}"></add-user-to-role>

            <permission-list v-if="role.permissions.length > 0 && showPermissions"
                    v-show="expanded[index]"
                    :permissions="role.permissions"
                    :user-group="role.name"
                    :canRemove="canRemovePermissions"
                    cssClass="members"></permission-list>
        </li>
    </ul>
</template>

<script>
    //TODO: sort out cssClass, derived from users - do we need that??
    //TODO: replace 'hasMembers' with computed 'listHasMembers'
    import Vue from "vue";
    import UserList from "./userList.vue";
    import PermissionList from "./permissionList.vue"
    import RemovePermission from "./removePermission";
    import AddUserToRole from "../admin/addUserToRole";

    export default {
        name: 'roleList',
        props: ["roles", "showMembers", "showPermissions", "canRemoveRoles", "canRemoveMembers", "canAddMembers",
                    "canRemovePermissions", "permission", "availableUsers"],
        data() {
            return {
                expanded: {}
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            },
            removed: function(email,role) {
                this.$emit('removed', email, role.name, this.permission)
            },
            addedUserToRole: function(email, role) {
                this.$emit('added-user-to-role', email, role.name)
            },
            availableUsersForRole: function(role) {
                return this.availableUsers.filter(u => role.members.map(m => m.email).indexOf(u) < 0)
            }
        },
        components: {
            RemovePermission,
            UserList,
            AddUserToRole,
            PermissionList
        }
    };
</script>
