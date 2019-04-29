<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetails" -->
<#-- @ftlvariable name="reportJson" type="String" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="/css/report-page.min.css"/>
    </#macro>
    <div class="row">
        <div class="col-12 col-md-4 col-xl-3 sidebar">
            <ul class="list-unstyled mb-0">
                <li class="nav-item">
                    <a class="nav-link active" data-toggle="tab" href="#report-tab" role="tab">Report</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#downloads-tab" role="tab">Downloads</a>
                </li>
            </ul>
            <hr/>
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
        <!-- TODO: Include this using gulp once merged with mrc-231 - just need it to make the tabs work -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
    </#macro>
</@layoutwide>
