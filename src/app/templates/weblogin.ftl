<#-- @ftlvariable name="authProvider" type="String" -->
<@layout>
    <div class="text-center p-3">
        <a class="btn btn-success btn-xl"
           href="/weblogin/external?requestedUrl=${requestedUrl}">Log in with <br/>${authProvider}</a>
    </div>
</@layout>