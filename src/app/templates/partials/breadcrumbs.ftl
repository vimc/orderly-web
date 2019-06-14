<div class="breadcrumbs">
    <div class="pl-md-1"></div>
    <#list breadcrumbs as crumb>
        <div class="crumb-item">
            <#if crumb.url?has_content>
                <a href="${appUrl}/${crumb.url}">${crumb.name}</a>
            <#else>
                <span>${crumb.name}</span>
            </#if>
        </div>
    </#list>
</div>