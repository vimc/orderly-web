<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/admin.min.css"/>
    </#macro>
    <div id="adminVueApp">
        <div class="row">
            <div class="col">
                <label class="font-weight-bold d-block">Manage roles</label>
                <manage-roles></manage-roles>
            </div>
            <div class="col">
                <label class="font-weight-bold d-block">Manage permissions</label>

                <hr/>
                <label class="font-weight-bold d-block">For individual users</label>
                <manage-users></manage-users>
            </div>
        </div>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/admin.bundle.js"></script>
    </#macro>
</@layout>
