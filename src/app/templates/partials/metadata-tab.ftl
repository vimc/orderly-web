<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<#-- @ftlvariable name="startTimeString" type="String" -->
<#-- @ftlvariable name="elapsedString" type="String" -->

<#include "report-title.ftl">
<div class="container ml-0">
    <div class="row">
        <div class="col-2 text-right">
            Started:
        </div>
        <div class="col-4">
            ${startTimeString}
        </div>
        <div class="col-2 text-right">
            Elapsed:
        </div>
        <div class="col-4">
            ${elapsedString}
        </div>
    </div>
</div>
