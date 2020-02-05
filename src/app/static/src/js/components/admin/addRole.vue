<template>
    <div>
        <div class="input-group input-group-sm">
            <input v-model="role" type="text" placeholder="role name" class="form-control">
            <button v-on:click="add" type="submit" id="add-role-btn" class="btn btn-sm" :disabled="!role">Add role</button>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'addRole',
        props: ['error', 'defaultMessage'],
        data() {
            return {
                role: ""
            }
        },
        methods: {
            add: function() {
                this.$emit('added', this.role);
            }
        }
    }
</script>
