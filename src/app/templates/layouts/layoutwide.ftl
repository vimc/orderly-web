<#macro layoutwide>
    <#include "../partials/header.ftl">
    <div class="container-fluid pt-5">
        <div class="row">
            <div id="content" class="col-12">
                <#nested>
            </div>
        </div>
    </div>
    <#include "../partials/footer.ftl">
</#macro>
