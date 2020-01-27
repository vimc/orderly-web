<template>
    <ul class="list-unstyled roles" v-if="roles.length > 0">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}, {'has-members': role.members.length > 0}]">
            <div class="expander" v-on:click="toggle(index)"></div>
            <span v-text="role.name" class="role-name" v-on:click="toggle(index)"></span>

            <span v-if="canRemoveRoles" v-on:click="function(){removeRole(role.name)}"
                  class="remove-user-group d-inline-block ml-2 large">×</span>

            <user-list v-if="role.members.length > 0"
                       v-show="expanded[index]"
                       cssClass="members"
                       :users="role.members"
                       :canRemove="canRemoveMembers"
                       @removed="function(email){removeMember(role.name, email)}"></user-list>

            <error-info :default-message="defaultMessage" :api-error="error"></error-info>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import UserList from "./userList.vue";
    import ErrorInfo from "../errorInfo.vue";
    import {api} from "../../utils/api";

    export default {
        name: 'roleList',
        props: ["roles", "canRemoveRoles", "canRemoveMembers", "permission"],
        data() {
            return {
                error: null,
                defaultMessage: "Something went wrong",
                expanded: {}
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            },
            removeMember: function (roleName, email) {
                api.delete(`/user-groups/${encodeURIComponent(roleName)}/user/${encodeURIComponent(email)}`)
                    .then(() => {
                        this.$emit('removed', roleName, email);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.error = error;
                    });
            },
            removeRole: function (roleName) {
                const data = {
                    ...this.permission,
                    action: "remove"
                };
                api.post(`/user-groups/${encodeURIComponent(roleName)}/actions/associate-permission/`, data)
                    .then(() => {
                        this.$emit("removed", roleName);
                        this.error = null;
                    })
                    .catch((error) => {
                        this.error = error;
                    });
            }
        },
        components: {
            ErrorInfo,
            UserList
        }
    };
</script>
