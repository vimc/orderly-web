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
    import Vue from "vue";
    import UserList from "../permissions/userList";
    import RoleList from "../permissions/roleList";

    export default {
        name: 'globalReadersList',
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
                api.get(`/user-groups/report-readers/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            },
            toggle: function (index, role) {
                Vue.set(this.roles, index, {...role, expanded: !role.expanded});
            }
        },
        components: {
            RoleList,
            UserList,
            EditIcon
        }
    };
</script>