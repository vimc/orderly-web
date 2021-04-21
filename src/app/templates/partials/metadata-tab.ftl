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
    <#if report.gitBranch?? || report.gitCommit??>
        <hr id="git-hr"/>
    </#if>
    <#if report.gitBranch??>
        <div id="git-branch-row" class="row">
            <div id="git-branch-label" class="col-2 text-right">
                Git branch:
            </div>
            <div id="git-branch-value" class="col-4">
                ${report.gitBranch}
            </div>
        </div>
    </#if>
    <#if report.gitCommit??>
        <div id="git-commit-row" class="row">
            <div id="git-commit-label" class="col-2 text-right">
                Git commit:
            </div>
            <div id="git-commit-value" class="col-4">
                ${report.gitCommit}
            </div>
        </div>
    </#if>
    <div id="reportDependenciesVueApp">
        <report-dependencies :report=report></report-dependencies>
    </div>
</div>
