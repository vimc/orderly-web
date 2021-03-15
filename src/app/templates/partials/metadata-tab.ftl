<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionWithDescLatestElapsed" -->
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
    <hr/>
    <div class="row">
        <div id="git-branch-label" class="col-2 text-right">
            Git branch:
        </div>
        <div id="git-branch-value" class="col-4">
            ${report.gitBranch}
        </div>
    </div>
    <div class="row">
        <div id="git-commit-label" class="col-2 text-right">
            Git commit:
        </div>
        <div id="git-commit-value" class="col-4">
            ${report.gitCommit}
        </div>
    </div>
</div>
