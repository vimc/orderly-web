<template>
    <div>
        <div class="input-group input-group-sm">
            <input v-model="role" type="text" placeholder="role name" class="form-control">
            <button id="add-role-btn" type="submit" class="btn btn-sm" :disabled="!role" @click="add">
                Add role
            </button>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'AddRole',
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
