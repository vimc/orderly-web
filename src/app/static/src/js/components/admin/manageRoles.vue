<template>
    <div id="manage-roles">
        <role-list :can-remove-members="true"
                   :can-add-members="true"
                   :can-remove-roles="false"
                   :can-delete-roles="true"
                   :roles="roles"
                   :available-users="typeaheadEmails"
                   @deleted="$emit('changed')"
                   @added="$emit('changed')"></role-list>
        <add-role @added="roleAdded" :error="addRoleError" :default-message="addRoleDefaultMessage"></add-role>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";
    import AddRole from "./addRole";

    export default {
        name: 'manageRoles',
        props: ["roles"],
        mounted() {
            this.getTypeaheadEmails();
        },
        data() {
            return {
                typeaheadEmails: [],
                addRoleError: "",
                addRoleDefaultMessage: ""
            }
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
                        this.addRoleError = "";
                        this.addRoleDefaultMessage = "";
                        this.$emit('changed')
                    })
                    .catch((error) => {
                        this.addRoleError = error;
                        this.addRoleDefaultMessage = `could not add role '${role}'`
                    });
            }
        },
        components: {
            RoleList,
            AddRole
        }
    };
</script>