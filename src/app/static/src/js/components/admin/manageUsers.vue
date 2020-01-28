<template>
    <div id="user-list">
        <input type="text"
               class="form-control"
               v-model="searchStr"
               placeholder="type to search"/>
        <ul class="list-unstyled roles mt-2">
            <li v-for="(u, index) in filteredUsers"
                v-bind:class="['role', {'open':expanded[index]}, {'has-children': u.permissions.length > 0}]">
                <div class="expander" v-on:click="toggle(index)"></div>
                <span v-on:click="toggle(index)" class="role-name">{{u.display_name}}</span>
                <div class="text-muted small email role-name">{{u.email}}</div>

                <permission-list v-if="u.permissions.length > 0"
                                 v-show="expanded[index]"
                                 :permissions="u.permissions"
                                 :email="u.email"
                                 @removed="function(p) {removePermission(p, u)}"></permission-list>
            </li>
        </ul>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import Vue from "vue";
    import PermissionList from "./permissionList.vue";

    export default {
        name: 'manageUsers',
        mounted() {
            this.getUsers();
        },
        data() {
            return {
                allUsers: [],
                searchStr: "",
                expanded: {}
            }
        },
        computed: {
            filteredUsers: function () {
                return this.allUsers.filter(u => this.userMatches(u, this.searchStr))
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            },
            getUsers: function () {
                api.get(`/users/`)
                    .then(({data}) => {
                        this.allUsers = data.data
                    })
            },
            removePermission: function (permission, user) {
                 user.permissions.splice(user.permissions.indexOf(permission), 1);
            },
            userMatches: function (u, searchStr) {
                return searchStr.length > 1 && (this.stringMatches(u.display_name, searchStr) ||
                    this.stringMatches(u.email, searchStr) || this.stringMatches(u.username, searchStr))
            },
            stringMatches: function (a, b) {
                return a.toLowerCase().indexOf(b.toLowerCase()) > -1
            }
        },
        components: {
            PermissionList
        }
    };
</script>
