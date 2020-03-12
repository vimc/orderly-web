<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<#-- @ftlvariable name="focalArtefactUrl" type="String" -->

<#include "report-title.ftl">
<#if parameterValues??>
    <p id="param-values"><span class="text-muted">Parameter values:</span> ${parameterValues}</p>
</#if>
<div id="reportTagsVueApp" class="mb-2">
    <report-tags :report=report :can-edit="${isReviewer?c}"></report-tags>
</div>
<#if focalArtefactUrl?has_content>
    <iframe src="${focalArtefactUrl}"
            width="100%" height="600px" class="border border-dark p-3"></iframe>

    <div class="text-right">
        <a target="_blank" href="${focalArtefactUrl}">
            View fullscreen
        </a>
    </div>
</#if>
