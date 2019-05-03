<#-- @ftlvariable name="authProvider" type="String" -->
<@layout>
    <div class="card mx-auto" style="width:18rem;">
        <div class="text-center p-3">
            <a class="login-external-link" href="/weblogin/external?requestedUrl=${requestedUrl}">Log in with <br/>${authProvider}</a>
        </div>
    </div>
</@layout>