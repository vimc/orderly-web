<#macro layout>
    <#include "../partials/header.ftl">
    <div class="container-fluid pt-5">
        <div class="row">
            <div class="col-12 col-lg-10 offset-lg-1">
                <#nested>
            </div>
        </div>
    </div>
    <#include "../partials/footer.ftl">
</#macro>