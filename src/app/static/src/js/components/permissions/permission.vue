<template>
    <div class="removable-user">
        <span class="display-name">{{displayName}}</span>
        <remove-permission v-if="canRemove"
                           :user-group="userGroup"
                           :permission="permission"
                           @removed="$emit('removed', permission)">
        </remove-permission>
    </div>
</template>

<script>
    import RemovePermission from "./removePermission";

    export default {
        name: "permission",
        components: {RemovePermission},
        props: ["canRemove", "permission", "userGroup"],
        computed: {
            displayName: function() {
                return `${this.permission.value}/${this.permission.name}`;
            }
        }
    }
</script>
