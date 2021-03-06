<#-- @ftlvariable name="appName" type="String" -->
<#-- @ftlvariable name="loggedIn" type="Boolean" -->
<#-- @ftlvariable name="isGuest" type="Boolean" -->
<#-- @ftlvariable name="user" type="String" -->
<#-- @ftlvariable name="logo" type="String" -->
<#-- @ftlvariable name="appUrl" type="String" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <#if styles??>
        <@styles></@styles>
    <#else>
        <link rel="stylesheet" type="text/css" href="${appUrl}/css/style.css">
    </#if>
    <link rel="icon" href="${appUrl}/favicon.ico" type="image/ico"/>
    <link rel="shortcut icon" href="${appUrl}/favicon.ico" type="image/x-icon"/>
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

    <div class="nav-right dropdown float-right">
        <a id="accessibility-link" href="${appUrl}/accessibility">Accessibility</a>
        <#if loggedIn && !isGuest>
            |
            <a id="logged-in" class="dropdown-toggle" href="#" data-toggle="dropdown">
                Logged in as ${user}
            </a>
            <div class="dropdown-menu dropdown-menu-right">
                <#if isAdmin && fineGrainedAuth>
                <a class="dropdown-item" href="${appUrl}/manage-access">Manage access</a>
                </#if>
                <#if isReviewer || !fineGrainedAuth>
                <a class="dropdown-item" href="${appUrl}/publish-reports">Publish reports</a>
                </#if>
                <a id="logout-link" class="dropdown-item" <#if authProvider?lower_case == "montagu">href="#" onclick="logoutViaMontagu()"<#else>href="${appUrl}/logout"</#if>>Logout</a>
            </div>
        </#if>
        <#if isGuest>
            |
            <a id="login" href="${appUrl}/weblogin">Login</a>
        </#if>
    </div>
</header>
<#include "breadcrumbs.ftl">
