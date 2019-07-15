<template>
    <ul class="list-unstyled roles">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}]"
            v-on:click="toggle(index)">
            <div class="expander"></div>
            <span v-text="role.name" class="role-name"></span>
            <remove-permission v-if="canRemoveRoles"
                               :user-group="role.name"
                               :permission="permission"
                               @removed="$emit('removed')"></remove-permission>
            <user-list v-on:click="function(e){e.stopPropagation()}"
                       v-show="expanded[index]"
                       cssClass="members"
                       :users="role.members"
                       :canRemove="canRemoveMembers"
                       @removed="$emit('removed')"></user-list>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import UserList from "./userList.vue";
    import RemovePermission from "./removePermission";

    export default {
        name: 'roleList',
        props: ["roles", "canRemoveRoles", "canRemoveMembers", "permission"],
        data() {
            return {
                expanded: {}
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            }
        },
        components: {
            RemovePermission,
            UserList
        }
    };
</script>
