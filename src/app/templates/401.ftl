<#-- @ftlvariable name="appEmail" type="String" -->
<#-- @ftlvariable name="appName" type="String" -->
<#-- @ftlvariable name="authProvider" type="String" -->
<@layout>
    <h1>Login failed</h1>
    <#if authProvider == "montagu">
        <p>We have not been able to successfully identify you as a Montagu user.</p>
    </#if>
    <#if authProvider == "github">
        <p>We have not been able to successfully identify you as a member of the app's configured Github org.
            If you think this is a mistake you should check the following steps have been taken</p>
        <ol>
            <li><a href="https://help.github.com/en/articles/requesting-organization-approval-for-oauth-apps">
                    GitHub organization approval for ${appName} has been requested
                </a></li>
            <li><a href="https://help.github.com/en/articles/approving-oauth-apps-for-your-organization">
                    GitHub organization approval for ${appName} has been granted</a>
            </li>
        </ol>
    </#if>
</@layout>
