<#-- @ftlvariable name="appName" type="String" -->
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
</header>