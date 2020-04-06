<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/documents.min.css"/>
    </#macro>

    <div id="app">
        <document-list :docs="docs" :can-manage="canManage"></document-list>
    </div>

    <#macro scripts>
        <script type="text/javascript">
            var docs = ${documentList};
            <#if canManage>
                var canManage = true;
            <#else>
                var canManage = false;
            </#if>
        </script>

        <script type="text/javascript" src="${appUrl}/js/documents.bundle.js"></script>
    </#macro>
</@layout>
