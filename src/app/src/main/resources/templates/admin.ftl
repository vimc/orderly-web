<#-- @ftlvariable name="usersJsonArray" type="String" -->
<@layout>
<h1>Users</h1>
<div id="vueApp">
    <ul v-for="user in users">
        <li class="form-group">
            <span v-text="user.username"></span> |
            <span v-text="user.email"></span> |
            <a v-bind:href="user.url">edit</a>
        </li>
    </ul>
    <form class="row">
        <div class="input-group mb-3 col-4">
            <input type="text" v-model="newUser" class="form-control" placeholder="Username">
            <div class="input-group-append">
                <button class="btn submit" type="button"
                        v-on:click="addUser">Add</button>
            </div>
        </div>
        <div v-if="error.length < 0" class="alert alert-danger">
            <span v-text="error"></span>
        </div>
    </form>
</div>

    <#macro scripts>
<script>
    var users = ${usersJsonArray}
</script>
<script src="/js/admin.bundle.js"></script>
    </#macro>
</@layout>