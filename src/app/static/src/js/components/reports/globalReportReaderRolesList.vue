<template>
    <div id="report-readers-global-list">
        <label class="font-weight-bold">
            Global read access
            <a href="#" class="small" data-toggle="tooltip" title="Coming soon!">
                <edit-icon></edit-icon>
                Edit roles</a>
        </label>
        <div>
            <role-list :can-remove-members="false"
                       :can-remove-roles="false"
                       :roles="roles"></role-list>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import EditIcon from './editIcon.vue';
    import UserList from "../permissions/userList.vue";
    import RoleList from "../permissions/roleList.vue";

    export default {
        name: 'globalReaderRolesList',
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
                api.get(`/roles/report-readers/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            }
        },
        components: {
            RoleList,
            UserList,
            EditIcon
        }
    };
</script>