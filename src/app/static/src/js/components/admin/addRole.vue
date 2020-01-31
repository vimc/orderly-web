<template>
    <div>
        <div class="input-group input-group-sm">
            <input v-model="role" type="text" placeholder="role name" class="form-control">
            <button v-on:click="add" type="submit" id="add-role-btn" class="btn btn-sm" :disabled="!role">Add role</button>
        </div>
        <error-info :default-message="defaultMessage" :api-error="error"></error-info>
    </div>
</template>

<script>
    import ErrorInfo from "../errorInfo.vue";
    export default {
        name: 'addRole',
        props: ['error', 'defaultMessage'],
        data() {
            return {
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
