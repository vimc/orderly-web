<template>
    <div>
        <ul v-if="permissions.length > 0" class="list-unstyled children mt-1">
            <li v-for="p in sortedPermissions" :key="p.name + (p.scope_prefix ? p.scope_id : '') + p.source">
                <span :id="p.name" class="name" :class="{'text-muted': !canRemovePermission(p)}">
                    {{ p.name }} <span v-if="p.scope_prefix">/ {{ p.scope_prefix }}:{{ p.scope_id }}</span>
                </span>
                <span v-if="canRemovePermission(p)" class="remove d-inline-block ml-2 large"
                      @click="function() {remove(p)}">×</span>
                <span v-if="!isDirect(p)" class="text-muted source small">(via {{ p.source }})</span>
            </li>
        </ul>
        <add-permission v-if="canEdit"
                        :user-group="userGroup"
                        :available-permissions="availablePermissions"
                        @added="add"></add-permission>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import ErrorInfo from "../errorInfo";
    import AddPermission from "./addPermission";
    import {api} from "../../utils/api";

    export default {
        name: "PermissionList",
        components: {AddPermission, ErrorInfo},
        props: ["permissions", "userGroup", "canEdit"],
        data() {
            return {
                error: "",
                defaultMessage: "",
                allPermissions: []
            }
        },
        computed: {
            sortedPermissions() {
                return [...this.permissions].sort(this.sortByName)
            },
            globalPermissions() {
                return this.permissions.filter(p => p.source === this.userGroup && !p.scope_id).map(p => p.name)
            },
            availablePermissions() {
                return this.allPermissions.filter(p => this.globalPermissions.indexOf(p) === -1)
            }
        },
        mounted() {
            this.getPermissions();
        },
        methods: {
            getPermissions: function () {
                api.get(`/typeahead/permissions/`)
                    .then(({data}) => {
                        this.allPermissions = data.data
                    })
            },
            remove(p) {
                this.$emit("removed", p);
            },
            add(p) {
                this.$emit("added", p);
            },
            canRemovePermission(p) {
                return this.canEdit && this.isDirect(p)
            },
            isDirect(p) {
                return p.source === this.userGroup
            },
            sortByName(a, b) {
                if (a.name > b.name) {
                    return 1;
                }
                if (a.name < b.name) {
                    return -1;
                }
                if (a.scope_id > b.scope_id) {
                    return 1;
                }
                if (a.scope_id < b.scope_id) {
                    return -1;
                }
                return 0;
            }
        }
    }
</script>
