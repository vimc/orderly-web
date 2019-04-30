<#macro render file>
    <#-- @ftlvariable name="file" type="List<org.vaccineimpact.orderlyweb.controllers.web.DownloadableFileViewModel>" -->
    <a target="_blank" href="${file.url}">${file.fileName} <#include "../partials/download-icon.ftl"></a>
</#macro>