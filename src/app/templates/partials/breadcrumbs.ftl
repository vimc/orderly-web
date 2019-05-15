<div class="breadcrumbs">
    <div class="pl-md-1"></div>
    <#list breadcrumbs as crumb>
        <div class="breadcrumb-item">
            <#if crumb.url?has_content>
                <a href="${crumb.url}">${crumb.name}</a>
            <#else>
                <span>${crumb.name}</span>
            </#if>
        </div>
    </#list>
</div>