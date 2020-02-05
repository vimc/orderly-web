<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/style.min.css"/>
    </#macro>

    <#list docs as doc>
        <#if doc.isFile>
           <a href="${doc.path}">${doc.displayName}</a>
        </#if>
    </#list>
</@layout>
