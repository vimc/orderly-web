<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/style.min.css"/>
    </#macro>
    <h3>Manage roles and permissions</h3>
    <div id="adminVueApp">
        <admin></admin>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/admin.bundle.js"></script>
    </#macro>
</@layout>
