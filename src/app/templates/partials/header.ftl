<#-- @ftlvariable name="appName" type="String" -->
<#-- @ftlvariable name="loggedIn" type="Boolean" -->
<#-- @ftlvariable name="user" type="String" -->
<#-- @ftlvariable name="logo" type="String" -->
<head>
    <#if styles??>
        <@styles></@styles>
    <#else>
        <link rel="stylesheet" type="text/css" href="${appUrl}/css/style.css">
    </#if>

    <title>${appName}</title>
</head>
    <body>
<header class="header">
    <a href="/" class="home-link">
        <img src="${appUrl}/img/logo/${logo}" class="pl-md-1 logo" height="75" alt="${appName}"/>
    </a>
    <div class="site-title">
        <a href="${appUrl}">
            ${appName}
        </a>
    </div>
    <#if loggedIn>
        <div class="logout">
            <span>Logged in as ${user} | <a id="logout-link"
              <#if authProvider?lower_case == "montagu">
                href="#" onclick="logoutViaMontagu()"
              <#else>
                href="${appUrl}/logout"
              </#if>>Logout</a></span>
        </div>
    </#if>
</header>
<#include "breadcrumbs.ftl">