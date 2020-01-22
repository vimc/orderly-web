<template>
    <ul v-bind:class="['list-unstyled', 'removable-users-list', cssClass]" v-if="users.length > 0">
        <li v-for="user in users" v-bind:id="user.email">
            <user :email="user.email"
                  :display-name="user.display_name"
                  :can-remove="canRemove"
                  :permission="permission"
                  :role="role"
                  @removed="removed"></user>
        </li>
    </ul>
</template>

<script>
    import User from "./user.vue";

    export default {
        name: "userList",
        components: {User},
        props: ["users", "canRemove", "cssClass", "permission", "role"],
        methods: {
            removed: function(email) {
                this.$emit('removed', email)
            }
        }
    }
</script>

