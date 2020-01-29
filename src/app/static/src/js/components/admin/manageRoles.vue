<template>
    <div id="role-list">
        <role-list :can-remove-members="true"
                   :can-add-members="true"
                   :can-remove-roles="false"
                   :roles="roles"
                   :available-users="typeaheadEmails"
                   @removed="getRoles"
                   @added="getRoles"></role-list>
        <add-role @added="roleAdded" :error="addRoleError"></add-role>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";
    import AddRole from "./addRole";

    export default {
        name: 'manageRoles',
        mounted() {
            this.getRoles();
            this.getTypeaheadEmails();
        },
        data() {
            return {
                roles: [],
                typeaheadEmails: [],
                addRoleError: ""
            }
        },
        methods: {
            getRoles: function () {
                api.get(`/roles/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            },
            getTypeaheadEmails: function () {
                api.get(`/typeahead/emails/`)
                    .then(({data}) => {
                        this.typeaheadEmails = data.data
                    })
            },
            roleAdded: function (role) {
                const data = {name: role};
                api.post(`/user-groups/`, data)
                    .then(() => {
                        this.getRoles();
                    })
                    .catch((error) => {
                        //TODO: deal with error strings, deal with duplicates
                        this.addRoleError = error;
                    });
            }
        },
        components: {
            RoleList,
            AddRole
        }
    };
</script>