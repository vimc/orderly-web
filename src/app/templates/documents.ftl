<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/documents.min.css"/>
    </#macro>

    <#macro listDoc doc>
        <#if doc.file>
            <a href="${appUrl}/documents/${doc.path}">${doc.displayName}</a>
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
