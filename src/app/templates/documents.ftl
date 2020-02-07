<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/style.min.css"/>
    </#macro>

    <#macro listDoc doc>

        <#if doc.isFile == true> <a href="${doc.path}">${doc.displayName}</a>
        </#if>

        <#list doc.children as child>
            <@listDoc child />
        </#list>

    </#macro>

    <#list docs as doc>
        <@listDoc doc></@listDoc>
    </#list>
</@layout>
