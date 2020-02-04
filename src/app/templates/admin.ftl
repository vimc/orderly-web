<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/admin.min.css"/>
    </#macro>
    <div id="adminVueApp">
        <admin-app></admin-app>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/admin.bundle.js"></script>
    </#macro>
</@layout>
