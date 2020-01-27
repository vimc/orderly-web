<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/admin.min.css"/>
    </#macro>
    <h1 class="h3">Manage roles</h1>
    <div id="adminVueApp">
        <manage-roles></manage-roles>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/admin.bundle.js"></script>
    </#macro>
</@layout>
