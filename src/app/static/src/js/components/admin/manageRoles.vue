<template>
    <div id="role-list">
        <role-list :can-remove-members="true"
                   :can-add-members="true"
                   :can-remove-roles="false"
                   :roles="roles"
                   :available-users="typeaheadEmails"
                   @removed="getRoles"
                   @added="getRoles"></role-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'manageRoles',
        mounted() {
            this.getRoles();
            this.getTypeaheadEmails();
        },
        data() {
            return {
                roles: [],
                typeaheadEmails: []
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
            }
        },
        components: {
            RoleList
        }
    };
</script>