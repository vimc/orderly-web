<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/style.min.css"/>
        <style>
            ul li {
                position: relative;
            }

            ul {
                padding-left: 30px;
                position: relative;
                list-style: none;
            }

            ul li:before {
                content: '';
                position: absolute;
                border-right: 2px solid black;
                border-bottom: 2px solid black;
                width: 7px;
                height: 7px;
                top: 11px;
                left: -15px;
                transform: translateY(-50%) rotate(-45deg);
            }
        </style>
    </#macro>

    <#macro listDoc doc>
        <#if doc.show>
            <#if doc.file>
                <a href="${doc.path}">${doc.displayName}</a>
            <#else>
                <span>${doc.displayName}</span>
            </#if>

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
