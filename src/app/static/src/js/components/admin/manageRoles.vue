<template>
    <div id="role-list">
        <role-list :can-remove-members="true"
                   :can-add-members="true"
                   :can-remove-roles="false"
                   :roles="roles"
                   :available-users="typeaheadEmails"
                   @removed="$emit('changed')"
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
                        this.$emit('added')
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