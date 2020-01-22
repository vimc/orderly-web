<template>
    <div class="removable-user">
        <span class="display-name">{{displayName}}</span>
        <remove-permission v-if="canRemove && permission"
                           :user-group="email"
                           :permission="permission"
                           @removed="$emit('removed', email)">
        </remove-permission>
        <remove-role v-if="canRemove && role"
                           :email="email"
                           :role="role"
                           @removed="$emit('removed', email)">
        </remove-role>
        <div class="text-muted small email">{{email}}</div>
    </div>
</template>

<script>
    import RemovePermission from "./removePermission";
    import RemoveRole from "./removeRole";

    export default {
        name: "user",
        components: {RemovePermission, RemoveRole},
        props: ["displayName", "email", "canRemove", "permission", "role"]
    }
</script>
