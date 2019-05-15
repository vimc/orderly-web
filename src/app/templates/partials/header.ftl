<#-- @ftlvariable name="appName" type="String" -->
<#-- @ftlvariable name="loggedIn" type="Boolean" -->
<#-- @ftlvariable name="user" type="String" -->
<head>
    <link rel="stylesheet" type="text/css" href="/css/style.css">
    <#if styles??>
        <@styles></@styles>
    </#if>
    <title>${appName}</title>
</head>
<body>
<header class="header">
    <a href="/">
        <img src="/img/logo.png" class="pl-md-1 logo" height="75" alt="${appName}"/>
    </a>
    <div class="site-title">
        <a href="/">
            ${appName}
        </a>
    </div>
    <#if loggedIn>
    <div class="logout">
        <span>Logged in as ${user} | <a href="/logout">Logout</a></span>
    </div>
    </#if>
</header>
<#include "breadcrumbs.ftl">