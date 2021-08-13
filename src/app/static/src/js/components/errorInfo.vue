<template>
    <div class="text-danger small error-message" v-if="errorMessage">
        {{errorMessage}}
    </div>
</template>

<script lang="ts">
    import Vue from "vue";

    export default Vue.extend({
        name: "errorInfo",
        props: ["apiError", "defaultMessage"],
        computed: {
            errorMessage: function () {
                if (this.apiError) {
                    return "Error: " + (this.apiErrorMessage(this.apiError.response) || this.defaultMessage);
                } else {
                    return null
                }
            }
        },
        methods: {
            apiErrorMessage: (response) => response &&
                response.data &&
                response.data.errors &&
                response.data.errors[0] &&
                response.data.errors[0].message
        }
    })
</script>
