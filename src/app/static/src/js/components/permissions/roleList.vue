<template>
    <ul class="list-unstyled roles" v-if="roles.length > 0">
        <li v-for="role in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[role.name]}, {'has-children': canAddMembers || (role.members.length > 0)}]">
            <div class="expander" v-on:click="toggle(role.name)"></div>
            <span v-text="role.name" v-on:click="toggle(role.name)" class="role-name"></span>

            <span v-if="canRemoveOrDeleteRole(role.name)"
                  v-on:click="function(){ canRemoveRoles ? removeRole(role.name) : confirmDeleteRole(role.name)}"
                  class="remove d-inline-block ml-2 large">×</span>


            <user-list v-if="role.members.length > 0"
                       v-show="expanded[role.name]"
                       cssClass="children"
                       :users="role.members"
                       :canRemove="canRemoveMembers"
                       @removed="function(email){removeMember(role.name, email)}"></user-list>

            <add-user-to-role v-if="canAddMembers && expanded[role.name]"
                        :role="role.name"
                        :available-users="availableUsersForRole(role)"
                        @added="$emit('added')"></add-user-to-role>

        </li>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <div v-if="canDeleteRoles" id="delete-role-confirm"
             v-bind:class="['modal-background', {'modal-hide':!showModal}, {'modal-show':showModal}]">
            <div class="modal-main px-3 py-3">
                <div class="mb-2 font-weight-bold">Confirm delete role</div>
                <div class="mb-2">Are you sure you want to delete {{roleToDelete}} role?</div>
                <div class="modal-buttons">
                    <button v-on:click="deleteRole(roleToDelete)" id="confirm-delete-btn" class="btn submit mr-3">Yes</button>
                    <button v-on:click="clearConfirmDelete()" id="cancel-delete-btn" class="btn btn-default">No</button>
                </div>
            </div>
        </div>
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
        props: ["roles", "canRemoveRoles", "canDeleteRoles", "canRemoveMembers", "canAddMembers", "availableUsers", "permission"],
        data() {
            return {
                error: null,
                defaultMessage: "Something went wrong",
                expanded: {},
                showModal: false,
                roleToDelete: ""
            }
        },
        methods: {
            toggle: function (roleName) {
                Vue.set(this.expanded, roleName, !this.expanded[roleName]);
            },
            availableUsersForRole: function(role) {
                return this.availableUsers.filter(u => role.members.map(m => m.email).indexOf(u) < 0)
            },
            canRemoveOrDeleteRole: function(role) {
                return this.canRemoveRoles || (this.canDeleteRoles && role !== "Admin")
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
            },
            confirmDeleteRole: function(roleName) {
                this.roleToDelete = roleName;
                this.showModal = true;
            },
            clearConfirmDelete: function() {
                this.roleToDelete = "";
                this.showModal = false;
            },
            deleteRole: function (roleName) {
                this.clearConfirmDelete();
                api.delete(`/roles/${encodeURIComponent(roleName)}/`)
                    .then(() => {
                        this.$emit("deleted", roleName);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not delete ${roleName}`;
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
