<template>
    <div id="report-readers-scoped-list">
        <div>
            <add-permission :permission="permission"
                            type="role"
                            :available-user-groups="availableRoles"
                            @added="getAllRoles"></add-permission>
            <role-list :can-remove-members="false"
                       :can-remove-roles="true"
                       :roles="currentRoles"></role-list>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import EditIcon from './editIcon.vue';
    import UserList from "../permissions/userList.vue";
    import RoleList from "../permissions/roleList.vue";
    import AddPermission from "../permissions/addPermission.vue";

    export default {
        name: 'scopedReaderRolesList',
        props: ["report"],
        mounted() {
            this.getCurrentRoles();
            this.getAllRoles();
        },
        data() {
            return {
                currentRoles: [],
                allRoles: []
            }
        },
        computed: {
            availableRoles: function () {
                return this.allRoles.filter(x =>
                    !(new Set(this.currentRoles.map(r => r.name))).has(x));
            },
            permission: function () {
                return {
                    name: "reports.read",
                    scope_prefix: "report",
                    scope_id: this.report.name
                };
            }
        },
        methods: {
            getAllRoles: function () {
                api.get(`/typeahead/roles/`)
                    .then(({data}) => {
                        this.allRoles = data.data
                    })
            },
            getCurrentRoles: function () {
                api.get(`/roles/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.currentRoles = data.data
                    })
            }
        },
        components: {
            RoleList,
            UserList,
            EditIcon,
            AddPermission
        }
    };
</script>