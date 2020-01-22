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

            <user-list v-if="role.members.length > 0"
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
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import UserList from "./userList.vue";
    import RemovePermission from "./removePermission";
    import AddUserToRole from "../admin/addUserToRole";

    export default {
        name: 'roleList',
        props: ["roles", "canRemoveRoles", "canRemoveMembers", "canAddMembers", "permission", "availableUsers"],
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
            AddUserToRole
        }
    };
</script>
