<template>
    <div class="text-danger small" v-if="errorMessage">
        {{errorMessage}}
    </div>
</template>

<script>
    import {api} from "../utils/api";

    export default {
        name: "errorInfo",
        props: ["error", "defaultMessage"],
        computed: {
            errorMessage: function () {
                if (this.error) {
                    return "Error: " + (api.errorMessage(this.error.response) || this.defaultMessage);
                }
                else {
                    return null
                }
            }
        }
    }
</script>
