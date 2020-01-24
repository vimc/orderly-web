<template>
    <ul v-bind:class="['list-unstyled', 'removable-users-list', cssClass]" v-if="permissions.length > 0">
        <li v-for="permission in permissions">
            <permission :permission="permission"
                        :user-group="userGroup"
                        :can-remove="canRemove"
                        @removed="removed"></permission>
        </li>
    </ul>
</template>

<script>
    import Permission from "./permission.vue";

    export default {
        name: "permissionList",
        components: {Permission},
        props: ["permissions", "userGroup", "canRemove", "cssClass"],
        methods: {
            removed: function(permission) {
                this.$emit('removed', permission)
            }
        }
    }
</script>

