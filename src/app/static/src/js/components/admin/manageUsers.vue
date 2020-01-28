<template>
    <div id="user-list">
        <input type="text"
               class="form-control"
               v-model="searchStr"
               placeholder="type to search"/>
        <ul>
            <li v-for="u in filteredUsers">
                {{u.display_name}}
            </li>
        </ul>
    </div>
</template>

<script>
    import {api} from "../../utils/api";

    export default {
        name: 'manageUsers',
        mounted() {
            this.getUsers();
        },
        data() {
            return {
                allUsers: [],
                searchStr: ""
            }
        },
        computed: {
            filteredUsers: function () {
                return this.allUsers.filter(u => this.userMatches(u, this.searchStr))
            }
        },
        methods: {
            getUsers: function () {
                api.get(`/users/`)
                    .then(({data}) => {
                        this.allUsers = data.data
                    })
            },
            userMatches: function (u, searchStr) {
                return searchStr.length > 1 && (this.stringMatches(u.display_name, searchStr) ||
                    this.stringMatches(u.email, searchStr) || this.stringMatches(u.username, searchStr))
            },
            stringMatches: function(a, b) {
                return a.toLowerCase().indexOf(b.toLowerCase()) > -1
            }
        }
    };
</script>
