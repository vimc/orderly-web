<#macro layout>
    <#include "../partials/header.ftl">
    <div class="container-fluid pt-md-5 pt-4">
        <div class="row">
            <div id="content" class="col-12 col-lg-10 offset-lg-1 pb-5">
                <#nested>
            </div>
        </div>
    </div>
    <#include "../partials/footer.ftl">
</#macro>