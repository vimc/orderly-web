<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.Report" -->
<#-- @ftlvariable name="reportJson" type="String" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="/css/report-page.min.css"/>
    </#macro>
    <div class="row">
        <div class="col-12 col-md-4 col-xl-3 sidebar">
            <ul class="list-unstyled mb-0">
                <li class="nav-item">
                    <a class="nav-link active" data-toggle="tab" href="#report" role="tab">Report</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#downloads" role="tab">Downloads</a>
                </li>
            </ul>
            <hr/>
            <publish-switch :report=report @toggle="handleToggle"></publish-switch>
        </div>
        <div class="col-12 col-md-8 col-xl-9 tab-content">
            <div class="tab-pane active" role="tabpanel" id="report">
                <#include "partials/report-tab.ftl">
            </div>
            <div class="tab-pane" role="tabpanel" id="downloads">
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
