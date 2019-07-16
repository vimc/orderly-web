<template>
    <ul class="list-unstyled roles">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}, {'has-members': role.members.length > 0}]"
            v-on:click="toggle(index)">
            <div class="expander"></div>
            <span v-text="role.name" class="role-name"></span>
            <user-list v-if="role.members.length > 0"
                       v-on:click="function(e){e.stopPropagation()}"
                       v-show="expanded[index]"
                       cssClass="members"
                       :users="role.members"
                       :canRemove="canRemoveMembers"></user-list>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import UserList from "./userList.vue";

    export default {
        name: 'roleList',
        props: ["roles", "canRemoveRoles", "canRemoveMembers"],
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
            UserList
        }
    };
</script>
