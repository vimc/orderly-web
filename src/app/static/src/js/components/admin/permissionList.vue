<template>
    <div>
        <ul v-if="permissions.length > 0" class="list-unstyled children mt-1">
            <li v-for="p in permissions">
                <span class="name">{{p.name}} <span
                        v-if="p.scope_prefix">/ {{p.scope_prefix}}:{{p.scope_id}}</span></span>
                <span v-on:click="function() {remove(p)}"
                      class="remove d-inline-block ml-2 large">×</span>
            </li>
        </ul>
        <add-permission :user-group="userGroup" :available-permissions="availablePermissions" @added="add"></add-permission>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import ErrorInfo from "../errorInfo";
    import AddPermission from "./addPermission";
    import {api} from "../../utils/api";

    export default {
        name: "permissionList",
        components: {AddPermission, ErrorInfo},
        props: ["permissions", "userGroup"],
        data() {
            return {
                error: "",
                defaultMessage: "",
                allPermissions: []
            }
        },
        mounted() {
            this.getPermissions();
        },
        computed: {
            globalPermissions() {
                return this.permissions.filter(p => !p.scope_id).map(p => p.name)
            },
            availablePermissions() {
                return this.allPermissions.filter(p => this.globalPermissions.indexOf(p) === -1)
            }
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
            }
        }
    }
</script>
