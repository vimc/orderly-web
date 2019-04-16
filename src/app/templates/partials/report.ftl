<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<#-- @ftlvariable name="focalArtefactUrl" type="String" -->

<h1 class="h2">${report.displayName}</h1>
<p class="small text-muted">${report.id}</p>

<iframe src="${focalArtefactUrl}"
        width="100%" height="600px" class="border border-dark p-3"></iframe>

<div class="text-right">
    <a target="_blank" href="${focalArtefactUrl}">
        View fullscreen
    </a>
</div>
