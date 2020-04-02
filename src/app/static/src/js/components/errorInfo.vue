<template>
    <div class="text-danger small" v-if="errorMessage">
        {{errorMessage}}
    </div>
</template>

<script>
    export default {
        name: "errorInfo",
        props: ["apiError", "defaultMessage"],
        computed: {
            errorMessage: function () {
                console.log(this.defaultMessage)
                console.log(this.apiError)
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
    }
</script>
