<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionWithDescLatestElapsed" -->
<#-- @ftlvariable name="startTimeString" type="String" -->
<#-- @ftlvariable name="elapsedString" type="String" -->
<#-- @ftlvariable name="instances" type="kotlin.collections.Map<String,String>" -->

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
    <#if report.gitBranch?? || report.gitCommit?? || instances?has_content>
        <hr id="git-hr"/>
        <div class="row">
            <div class="col">
                <#if report.gitBranch??>
                    <div id="git-branch-row" class="row">
                        <div id="git-branch-label" class="col-4 text-right">
                            Git branch:
                        </div>
                        <div id="git-branch-value" class="col-8">
                            ${report.gitBranch}
                        </div>
                    </div>
                </#if>
                <#if report.gitCommit??>
                    <div id="git-commit-row" class="row">
                        <div id="git-commit-label" class="col-4 text-right">
                            Git commit:
                        </div>
                        <div id="git-commit-value" class="col-8">
                            ${report.gitCommit}
                        </div>
                    </div>
                </#if>
            </div>
            <div class="col">
                <#list instances as instanceKey, instanceValue>
                    <div class="row db-instance-row">
                        <div class="col-4 text-right db-instance-label">
                            Database "${instanceKey}":
                        </div>
                        <div class="col-8 db-instance-value">
                            ${instanceValue}
                        </div>
                    </div>
                </#list>
            </div>
        </div>
    </#if>
    <div id="reportDependenciesVueApp">
        <report-dependencies :report=report></report-dependencies>
    </div>
</div>
