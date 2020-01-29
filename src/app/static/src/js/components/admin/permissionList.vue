<template>
    <div>
        <ul class="list-unstyled children mt-1">
            <li v-for="p in permissions">
                <span class="name">{{p.name}} <span
                        v-if="p.scope_prefix">/ {{p.scope_prefix}}:{{p.scope_id}}</span></span>
                <span v-on:click="function() {remove(p)}"
                      class="remove d-inline-block ml-2 large">×</span>
            </li>
        </ul>
        <add-permission :email="email" :available-permissions="availablePermissions"></add-permission>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo";
    import AddPermission from "./addPermission";

    export default {
        name: "permissionList",
        components: {AddPermission, ErrorInfo},
        props: ["allPermissions", "permissions", "email"],
        data() {
            return {
                error: "",
                defaultMessage: ""
            }
        },
        computed: {
            globalPermissions() {
                return this.permissions.filter(p => p.scope_id).map(p => p.name)
            },
            availablePermissions() {
                return this.allPermissions.filter(p => this.globalPermissions.indexOf(p) === -1)
            }
        },
        methods: {
            remove(p) {
                const data = {
                    action: "add",
                    ...p
                };

                api.post(`/user-groups/${encodeURIComponent(this.email)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.error = null;
                        this.$emit("removed", p);
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not remove ${p.name} from ${this.email}`;
                    });
            }
        }
    }
</script>
