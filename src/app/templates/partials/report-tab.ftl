<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<#-- @ftlvariable name="focalArtefactUrl" type="String" -->

<#include "report-title.ftl">
<div class="container ml-0 mb-2">
    <#if report.name != report.displayName>
        <div class="row">
            <div class="col-2 text-right text-muted">Name:</div>
            <div class="col-10" id="report-name">${report.name}</div>
        </div>
    </#if>
    <#if report.description??>
        <div class="row">
            <div class="col-2 text-right text-muted">Description:</div>
            <div class="col-10" id="report-description">${report.description}</div>
        </div>
    </#if>
    <#if parameterValues??>
        <div class="row">
            <div class="col-2 text-right text-muted">Parameter values:</div>
            <div class="col-10" id="report-parameters">${parameterValues}</div>
        </div>
    </#if>
</div>
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
