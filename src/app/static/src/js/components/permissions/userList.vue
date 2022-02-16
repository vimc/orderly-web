<template>
    <ul v-if="users.length > 0"
        :class="['list-unstyled', 'removable-users-list', cssClass]">
        <li v-for="user in users" :id="user.email" :key="user.email">
            <user :email="user.email"
                  :display-name="user.display_name"
                  :can-remove="canRemove"
                  @removed="removed"></user>
        </li>
    </ul>
</template>

<script>
    import User from "./user.vue";

    export default {
        name: "UserList",
        components: {User},
        props: ["users", "canRemove", "cssClass"],
        methods: {
            removed: function (email) {
                this.$emit('removed', email)
            }
        }
    }
</script>

