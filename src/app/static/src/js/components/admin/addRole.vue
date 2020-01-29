<template>
    <div class="input-group input-group-sm">
        <input v-model="role" type="text" placeholder="role name" class="form-control">
        <button v-on:click="add" type="submit" class="btn btn-sm" :disabled="!!!role">Add role</button>
    </div>
    <error-info :default-message="defaultMessage" :api-error="error"></error-info>
</template>

<script>
    import ErrorInfo from "../errorInfo.vue";
    export default {
        name: 'addRole',
        props: ['error'],
        data() {
            return {
                error: "",
                defaultMessage: "",
                role: ""
            }
        },
        components: {
            ErrorInfo
        },
        methods: {
            add: function() {
                this.$emit('added', this.role);
            }
        }
    }
</script>
