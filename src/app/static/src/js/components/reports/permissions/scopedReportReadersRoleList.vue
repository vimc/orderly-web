<template>
    <div>
        <manage-permissions :report="report"
                            placeholder="role name"
                            add-text="Add role"
                            :current-items="currentRoles"
                            :available-items="availableRoles"
                            @added="getCurrentRoles"></manage-permissions>
        <role-list :roles="currentRoles" :can-remove="true"></role-list>
    </div>
</template>

<script>
    import {api} from "../../../utils/api";
    import RoleList from "./roleList.vue";
    import ManagePermissions from "./managePermissions.vue";

    export default {
        name: 'scopedReadersRoleList',
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
        methods: {
            getCurrentRoles: function () {
                api.get(`/user-groups/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.currentRoles = data.data
                    })
            },
            getAllRoles: function () {
                api.get(`/user-groups/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.allRoles = ["funders"]
                    })
            }
        },
        computed: {
            availableRoles: function () {
                return this.allRoles.filter(x =>
                    !(new Set(this.currentRoles.map(r => r.name))).has(x));
            }
        },
        components: {
            ManagePermissions,
            RoleList
        }
    };
</script>