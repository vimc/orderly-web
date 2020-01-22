<template>
    <span>
     <span v-on:click="remove"
           class="remove-user-group d-inline-block ml-2 large">×</span>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </span>
</template>
<script>

    import {api} from "../../utils/api";
    import ErrorInfo from "../../components/errorInfo.vue"

    export default {
        name: 'removeRole',
        props: ["role", "email"],
        data() {
            return {
                error: null
            }
        },
        computed: {
            defaultMessage() {
                return `could not remove ${this.email}`;
            }
        },
        methods: {
            remove: function () {

                const data = {
                    ...this.role,
                    action: "remove"
                };

                api.delete(`/user-groups/${encodeURIComponent(this.role)}/user/${encodeURIComponent(this.email)}`, data)
                    .then(() => {
                        this.$emit("removed");
                        this.error = null;
                    })
                    .catch((error) => {
                        this.error = error;
                    });
            }
        },
        components: {
            ErrorInfo
        }
    };
</script>