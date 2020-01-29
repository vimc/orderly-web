<template>
    <div id="scoped-roles-list">
        <add-report-reader :report-name="report.name"
                           type="role"
                           :available-user-groups="availableRoles"
                           @added="getCurrentRoles"></add-report-reader>
        <role-list :can-remove-members="false"
                   :can-remove-roles="true"
                   :roles="currentRoles"
                   :permission="permission"
                   @removed="getCurrentRoles"></role-list>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";
    import AddReportReader from "../permissions/addReportReader";

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
                }
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
            AddReportReader,
            RoleList
        }
    };

</script>
