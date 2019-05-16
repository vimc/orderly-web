<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetails" -->
<#-- @ftlvariable name="reportJson" type="String" -->
<#-- @ftlvariable name="isAdmin" type="Boolean" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="/css/report-page.min.css"/>
    </#macro>
    <div class="row">
        <div class="col-12 col-md-4 col-xl-3 sidebar">
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link active" data-toggle="tab" href="#report-tab" role="tab">Report</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#downloads-tab" role="tab">Downloads</a>
                </li>
            </ul>
            <hr/>
            <#if isAdmin>
                <div id="publishSwitchVueApp">
                    <publish-switch :report=report @toggle="handleToggle"></publish-switch>
                </div>
            </#if>
            <#if isRunner>
                <div id="runReportVueApp">
                    <run-report :report=report ></run-report>
                </div>
            </#if>
        </div>
        <div class="col-12 col-md-8 tab-content">
            <div class="tab-pane active" role="tabpanel" id="report-tab">
                <#include "partials/report-tab.ftl">
            </div>
            <div class="tab-pane" role="tabpanel" id="downloads-tab">
                <#include "partials/downloads.ftl">
            </div>
        </div>
    </div>
    <#macro scripts>
        <script>
            var report = ${reportJson}
        </script>
        <script src="/js/report.bundle.js"></script>
    </#macro>
</@layoutwide>
