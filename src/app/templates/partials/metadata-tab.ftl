<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->

<#include "report-title.ftl">
<div class="container ml-0">
    <#if report.name != report.displayName>
        <div class="row">
            <div class="col-2 text-right">Name:</div>
            <div class="col-10"><strong>${report.name}</strong></div>
        </div>
    </#if>
    <#if report.description??>
        <div class="row">
            <div class="col-2 text-right">Description:</div>
            <div class="col-10">${report.description}</div>
        </div>
    </#if>
    <#if !(report.name != report.displayName || report.description??)>
        No relevant metadata
    </#if>
</div>
