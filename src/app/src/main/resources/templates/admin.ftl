<#-- @ftlvariable name="usersJsonArray" type="String" -->
<@layout>
<h1>Users</h1>
<div id="vueApp">
    <ul>
        <li class="form-group" v-for="user in users">
            <span v-text="user.username"></span> |
            <span v-text="user.email"></span> |
            <a v-bind:href="user.url">edit</a>
        </li>
    </ul>
    <div class="col-6">
        <h2>Add new user:</h2>
        <new-user-form @created="handleCreate"></new-user-form>
    </div>
</div>

    <#macro scripts>
<script>
    var users = ${usersJsonArray}
</script>
<script src="/js/admin.bundle.js"></script>
    </#macro>
</@layout>