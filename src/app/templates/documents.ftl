<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/documents.css"/>
    </#macro>

    <div id="app">
        <document-page :can-manage="canManage"></document-page>
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
