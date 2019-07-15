<template>
    <div>
     <span v-on:click="remove"
           class="remove-user-group d-inline-block ml-2 large">×</span>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>
<script>

    import {api} from "../../utils/api";
    import ErrorInfo from "../../components/errorInfo.vue"

    export default {
        name: 'removePermission',
        props: ["permission", "userGroup"],
        data() {
            return {
                error: null
            }
        },
        computed: {
            defaultMessage() {
                return `could not remove ${this.userGroup}`;
            }
        },
        methods: {
            remove: function () {

                const data = {
                    ...this.permission,
                    action: "remove"
                };

                api.post(`/user-groups/${encodeURIComponent(this.userGroup)}/actions/associate-permission/`, data)
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