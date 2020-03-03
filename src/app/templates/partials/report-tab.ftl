<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<#-- @ftlvariable name="focalArtefactUrl" type="String" -->

<#include "report-title.ftl">
<#if parameterValues??>
    <p id="param-values"><span class="text-muted">Parameter values:</span> ${parameterValues}</p>
</#if>
<#if focalArtefactUrl?has_content>
    <iframe src="${focalArtefactUrl}"
            width="100%" height="600px" class="border border-dark p-3"></iframe>

    <div class="text-right">
        <a target="_blank" href="${focalArtefactUrl}">
            View fullscreen
        </a>
    </div>
</#if>
