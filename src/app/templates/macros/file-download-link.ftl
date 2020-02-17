<#import "download-icon.ftl" as download_icon>

<#macro render file class="" iconColor="#007bff">
    <#-- @ftlvariable name="file" type="org.vaccineimpact.orderlyweb.controllers.web.DownloadableFileViewModel" -->
    <a target="_blank" class="${class}" href="${file.url}">${file.name} <@download_icon.render fill=iconColor /></a>
    <#if file.formattedSize??>
        <span class="text-muted small ml-1 file-size">(${file.formattedSize})</span>
    </#if>
</#macro>