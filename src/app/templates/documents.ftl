<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/documents.min.css"/>
    </#macro>

    <#macro listDoc doc>
        <#if doc.file>
            ${doc.displayName}
            <a href="${appUrl}/project-docs/${doc.path}?inline=true">open</a>/<a href="${appUrl}/project-docs/${doc.path}">download</a>
        <#else>
            <span>${doc.displayName}</span>
        </#if>
        <#if doc.children?size != 0>
            <ul>
                <#list doc.children as child>
                    <li><@listDoc child /></li>
                </#list>
            </ul>
        </#if>
    </#macro>

    <ul>
        <#list docs as doc>
            <li><@listDoc doc></@listDoc></li>
        </#list>
    </ul>
</@layout>
