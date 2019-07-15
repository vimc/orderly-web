<template>
    <div class="removable-user">
        <span class="display-name">{{displayName}}</span>
        <remove-permission v-if="canRemove"
                           :user-group="email"
                           :permission="permission"
                           @removed="$emit('removed')">
        </remove-permission>
        <div class="text-muted small email">{{email}}</div>
    </div>
</template>

<script>
    import RemovePermission from "./removePermission";

    export default {
        name: "user",
        components: {RemovePermission},
        props: ["displayName", "email", "canRemove", "permission"]
    }
</script>
