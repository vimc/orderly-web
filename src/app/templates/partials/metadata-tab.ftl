<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetail" -->
<#-- @ftlvariable name="startTimeString" type="String" -->
<#-- @ftlvariable name="elapsedString" type="String" -->

<#include "report-title.ftl">
<div class="container ml-0">
    <div class="row">
        <div id="started-label" class="col-2 text-right">
            Started:
        </div>
        <div id="started-value" class="col-4">
            ${startTimeString}
        </div>
        <div id="elapsed-label" class="col-2 text-right">
            Elapsed:
        </div>
        <div id="elapsed-value" class="col-4">
            ${elapsedString}
        </div>
    </div>
    <div id="reportDependenciesVueApp" class="row">
        <report-dependencies :report=report></report-dependencies>
    </div>
</div>
