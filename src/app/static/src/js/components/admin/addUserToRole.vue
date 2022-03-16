<template>
    <div class="mb-3 add-role mt-2">
        <typeahead v-model="newUser"
                   size="sm"
                   placeholder="email"
                   :data="availableUsers">
            <template slot="append">
                <button type="submit" class="btn btn-sm" @click="add">
                    Add user
                </button>
            </template>
        </typeahead>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import ErrorInfo from "../errorInfo.vue";
    import Typeahead from "../typeahead/typeahead.vue";

    export default {
        name: 'AddUserToRole',
        components: {
            ErrorInfo,
            Typeahead
        },
        props: ['role', 'availableUsers'],
        data() {
            return {
                newUser: "",
                error: "",
                defaultMessage: ""
            }
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