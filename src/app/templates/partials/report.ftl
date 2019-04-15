<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<h1 class="h2">${report.displayName!report.name}</h1>
<p class="small text-muted">${report.id}</p>
<#assign url="/reports/${report.name}/versions/${report.id}/artefacts/${report.artefacts[0].files[0]}?inline=true">

<iframe src="${url}"
        width="100%" height="600px" class="border border-dark p-3"></iframe>

<div class="text-right">
    <a target="_blank" href="${url}">
        View fullscreen
    </a>
</div>
