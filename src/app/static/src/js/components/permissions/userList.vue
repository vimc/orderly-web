<template>
    <ul v-bind:class="['list-unstyled', 'removable-users-list', cssClass]">
        <li v-for="user in users" v-bind:id="user.email">
            <user :email="user.email"
                  :display-name="user.display_name"
                  :can-remove="canRemove"
                  :permission="permission"
                  @removed="$emit('removed')"></user>
        </li>
    </ul>
</template>

<script>
    import User from "./user.vue";

    export default {
        name: "userList",
        components: {User},
        props: ["users", "canRemove", "cssClass", "permission"]
    }
</script>

