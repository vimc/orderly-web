<template>
    <div id="manage-roles">
        <role-list :can-remove-members="true"
                   :can-add-members="true"
                   :can-remove-roles="true"
                   :roles="roles"
                   :available-users="typeaheadEmails"
                   @removed="function(role){confirmDeleteRole(role)}"
                   @removedMember="$emit('changed')"
                   @added="$emit('changed')"></role-list>
        <add-role @added="roleAdded"></add-role>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        <div id="delete-role-confirm"
             :class="['modal-background', {'modal-hide':!showModal}, {'modal-show':showModal}]">
            <div class="modal-main px-3 py-3">
                <div class="mb-2 font-weight-bold">
                    Confirm delete role
                </div>
                <div class="mb-2">
                    Are you sure you want to delete {{ roleToDelete }} role?
                </div>
                <div class="modal-buttons">
                    <button id="confirm-delete-btn" class="btn submit mr-3" @click="deleteRole(roleToDelete)">Yes
                    </button>
                    <button id="cancel-delete-btn" class="btn btn-default" @click="clearConfirmDelete()">
                        No
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";
    import AddRole from "./addRole";
    import ErrorInfo from "../errorInfo"

    export default {
        name: 'ManageRoles',
        components: {
            RoleList,
            AddRole,
            ErrorInfo
        },
        props: ["roles"],
        data() {
            return {
                typeaheadEmails: [],
                error: "",
                defaultMessage: "",
                showModal: false,
                roleToDelete: ""
            }
        },
        mounted() {
            this.getTypeaheadEmails();
        },
        methods: {
            getTypeaheadEmails: function () {
                api.get(`/typeahead/emails/`)
                    .then(({data}) => {
                        this.typeaheadEmails = data.data
                    })
            },
            roleAdded: function (role) {
                const data = {name: role};
                api.post(`/roles/`, data)
                    .then(() => {
                        this.error = "";
                        this.defaultMessage = "";
                        this.$emit('changed')
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add role '${role}'`
                    });
            },
            confirmDeleteRole: function (roleName) {
                this.roleToDelete = roleName;
                this.showModal = true;
            },
            clearConfirmDelete: function () {
                this.roleToDelete = "";
                this.showModal = false;
            },
            deleteRole: function (roleName) {
                this.clearConfirmDelete();
                api.delete(`/roles/${encodeURIComponent(roleName)}/`)
                    .then(() => {
                        this.$emit("changed", roleName);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not delete ${roleName}`;
                        this.error = error;
                    });
            }
        }
    };
</script>