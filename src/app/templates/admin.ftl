<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/admin.css"/>
    </#macro>
    <div id="adminVueApp">
        <admin-app></admin-app>
    </div>
    <#macro scripts>
        <script type="text/javascript">
            let canAllowGuest = ${canAllowGuest?c};
        </script>
        <script type="text/javascript" src="${appUrl}/js/admin.bundle.js"></script>
    </#macro>
</@layout>
