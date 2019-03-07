<template>
    <form>
        <div class="form-group">
            <input type="text" v-model="username" class="form-control" placeholder="Username">
        </div>
        <div class="form-group">
            <input type="text" v-model="email" class="form-control" placeholder="Email">
        </div>
        <div class="form-group">
            <button class="btn submit" type="button"
                    v-on:click="addNewUser">Add
            </button>
        </div>
        <div v-if="error.length < 0" class="alert alert-danger">
            <span v-text="error"></span>
        </div>
    </form>
</template>

<script>
    import axios from "axios";

    export default {
        name: 'newUserForm',
        methods: {
            addNewUser: function () {
                axios.post('/admin/adduser', this.username).then(() => {
                    this.$emit('created', {username: this.username, email: this.email});
                    this.username = "";
                    this.email = "";
                }).catch(() => {
                        this.username = "";
                        this.email = "";
                        this.error = "An error occurred while adding a new user.";
                    }
                )
            }
        },
        data() {
            return {
                username: "",
                email: "",
                error: ""
            }
        }
    };
</script>