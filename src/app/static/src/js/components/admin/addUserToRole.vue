<template>
    <div class="mb-3 add-role mt-2">
        <vue-bootstrap-typeahead
                size="sm"
                v-model="newUser"
                placeholder="email"
                :data="availableUsers">
            <template slot="append">
                <button v-on:click="add" type="submit" class="btn btn-sm">Add user</button>
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
        name: 'addUserToRole',
        props: ['role', 'availableUsers'],
        data() {
            return {
                newUser: "",
                error: "",
                defaultMessage: ""
            }
        },
        components: {
            ErrorInfo,
            VueBootstrapTypeahead
        },
        watch: {
            newUser() {
                this.error = ""
            }
        },
        methods: {
            add: function () {
                if (!new Set(this.availableUsers).has(this.newUser)) {
                    const msg = `${this.newUser} is not an available user or already belongs to role`;
                    this.error = msg;
                    this.defaultMessage = msg;
                    return;
                }

                const data = {
                    email: this.newUser
                };

                api.post(`/roles/${encodeURIComponent(this.role)}/users/`, data)
                    .then(() => {
                        this.$emit("added");
                        this.newUser = "";
                        this.error = null;
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not add user`;
                    });
            }
        }
    };
</script>