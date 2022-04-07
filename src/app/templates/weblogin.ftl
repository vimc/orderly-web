<#-- @ftlvariable name="authProvider" type="String" -->
<@layout>
    <div id="external-login" class="text-center p-3">
        <a class="login-link btn btn-success btn-xl"
           href="${appUrl}/weblogin/external?requestedUrl=${requestedUrl}">Log in with <br/>${authProvider}</a>
    </div>
</@layout>