<template>
    <div>
        <div id="manage-roles" class="col-4 ">
            <h4>Role management</h4>
            <role-list :show-members="true"
                       :show-permissions="false"
                       :can-remove-members="true"
                       :can-add-members="true"
                       :can-remove-roles="false"
                       :roles="roles"
                       :available-users="typeaheadEmails"
                       @removed="removedMemberFromRole"
                       @added-user-to-role="added"></role-list>
        </div>
        <div id="manage-permissions" class="col-4 ">
            <h4>Permission management</h4>
            <h5>For roles</h5>
            <role-list :show-members="false"
                       :show-permissions="true"
                       :can-remove-permissions="true"
                       :roles="roles"
                       @removed="removedPermissionFromRole"></role-list>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'admin',
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
            removedMemberFromRole: function(email,roleName) {
                const role = this.roles.find(r => r.name === roleName);
                const memberIdx = role.members.findIndex(m => m.email === email);
                role.members.splice(memberIdx,1);
            },
            removedPermissionFromRole: function(permission, roleName) {
                alert("removed permission from " + roleName)
            },
            added: function() {
                this.getRoles();
            }
        },
        components: {
            RoleList
        }
    };
</script>