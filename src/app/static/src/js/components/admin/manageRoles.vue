<template>
    <div id="role-list" class="col-4 ">
        <role-list :can-remove-members="true"
                   :can-add-members="true"
                   :can-remove-roles="false"
                   :roles="roles"
                   :available-users="typeaheadEmails"
                   @removed="removed"
                   @added-user-to-role="added"></role-list>
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
            },
            added: function() {
                this.getRoles();
            },
            removed: function (roleName, email) {
                const role = this.roles.find(r => r.name === roleName);
                const memberIdx = role.members.findIndex(m => m.email === email);
                role.members.splice(memberIdx, 1);
            }
        },
        components: {
            RoleList
        }
    };
</script>