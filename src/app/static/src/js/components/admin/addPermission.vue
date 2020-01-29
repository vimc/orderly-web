<template>
    <div class="mb-3 ml-3">
        <vue-bootstrap-typeahead
                size="sm"
                v-model="newPermission"
                placeholder="name"
                :data="availablePermissions">
            <template slot="append">
                <button v-on:click="add" type="submit" class="btn btn-sm">Add</button>
            </template>
        </vue-bootstrap-typeahead>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import ErrorInfo from "../errorInfo.vue";

    export default {
        name: 'addPermission',
        props: ['email', 'availablePermissions'],
        data() {
            return {
                newPermission: "",
                error: "",
                defaultMessage: ""
            }
        },
        components: {
            ErrorInfo,
            VueBootstrapTypeahead
        },
        watch: {
            newPermission() {
                this.error = ""
            }
        },
        methods: {
            add: function () {
                if (!new Set(this.availablePermissions).has(this.newPermission)) {
                    const msg = `${this.newPermission} is not an available permission or already belongs to ${this.email}`;
                    this.error = msg;
                    this.defaultMessage = msg;
                    return;
                }

                const data = {
                    action: "remove",
                    ...this.newPermission
                };

                api.post(`/user-groups/${encodeURIComponent(this.email)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.error = null;
                        this.$emit("added", this.newPermission);
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add ${this.newPermission} to ${this.email}`;
                    });
            }
        }
    };
</script>
