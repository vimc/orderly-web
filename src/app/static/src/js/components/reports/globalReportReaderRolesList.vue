<template>
    <div id="report-readers-global-list">
        <role-list :can-remove-members="false"
                   :can-remove-roles="false"
                   :roles="roles"></role-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'GlobalReaderRolesList',
        components: {
            RoleList
        },
        data() {
            return {
                roles: []
            }
        },
        mounted() {
            this.getRoles();

        },
        methods: {
            getRoles: function () {
                api.get(`/roles/report-readers/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            }
        }
    };
</script>