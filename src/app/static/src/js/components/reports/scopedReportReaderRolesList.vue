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
                   @removed="function(roleName){removeRole(roleName)}"></role-list>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import RoleList from "../permissions/roleList.vue";
    import AddReportReader from "../permissions/addReportReader";
    import ErrorInfo from "../errorInfo";

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
                allRoles: [],
                error: "",
                defaultMessage: ""
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
            },
            removeRole: function (roleName) {
                const scopeId = this.permission.scope_id;
                const scopePrefix = this.permission.scope_prefix;
                const query = (scopeId && scopePrefix) ? `?scopePrefix=${scopePrefix}&scopeId=${scopeId}` : "";

                api.delete(`/roles/${encodeURIComponent(roleName)}/permissions/${this.permission.name}/${query}`)
                    .then(() => {
                        this.getCurrentRoles();
                        this.error = null;
                        this.defaultMessage = "";
                    })
                    .catch((error) => {
                        this.defaultMessage = `could not remove ${roleName}`;
                        this.error = error;
                    });
            }
        },
        components: {
            AddReportReader,
            RoleList,
            ErrorInfo
        }
    };

</script>
