<template>
    <div id="role-list">
        <role-list :can-remove-members="true"
                   :can-remove-roles="false"
                   :roles="roles"
                   @removed="removed"></role-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'manageRoles',
        mounted() {
            this.getRoles();

        },
        data() {
            return {
                roles: []
            }
        },
        methods: {
            getRoles: function () {
                api.get(`/roles/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
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