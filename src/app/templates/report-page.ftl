<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.ReportVersionDetails" -->
<#-- @ftlvariable name="reportJson" type="String" -->
<#-- @ftlvariable name="isAdmin" type="Boolean" -->
<#-- @ftlvariable name="isReviewer" type="Boolean" -->
<#-- @ftlvariable name="fineGrainedAuth" type="Boolean" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/report-page.min.css"/>
    </#macro>
    <div class="row">
        <div class="col-12 col-md-4 col-xl-3">
            <div class="sidebar pb-4 pb-md-0">
                <nav class="pl-0 pr-0 pr-md-4 navbar navbar-light">
                    <button type="button" class="d-md-none navbar-toggler" data-toggle="collapse"
                            data-target="#sidebar">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="d-md-block mt-4 mt-md-0 collapse navbar-collapse" id="sidebar">
                        <ul class="nav flex-column list-unstyled mb-0">
                            <li class="nav-item">
                                <a class="nav-link active" data-toggle="tab" href="#report-tab" role="tab">Report</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab" href="#downloads-tab" role="tab">Downloads</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab" href="#changelog-tab" role="tab">Changelog</a>
                            </li>
                        </ul>
                        <hr/>
                        <div class="pl-3">
                            <#include "partials/version-picker.ftl">
                            <#if isReviewer || !fineGrainedAuth>
                                <div id="publishSwitchVueApp" class="pt-3">
                                    <publish-switch :report=report @toggle="handleToggle"></publish-switch>
                                </div>
                            </#if>
                            <#if isAdmin && fineGrainedAuth>
                                <div id="reportReadersListVueApp" class="mt-5">
                                    <label class="font-weight-bold">
                                        Global read access
                                        <a href="${appUrl}/manage-access" class="small">
                                            <edit-icon></edit-icon>
                                            Edit roles</a>
                                    </label>
                                    <global-reader-roles-list></global-reader-roles-list>
                                    <global-report-readers-list></global-report-readers-list>

                                    <label class="font-weight-bold specific-read-access-label">Specific read access</label>
                                    <scoped-reader-roles-list :report=report></scoped-reader-roles-list>
                                    <hr/>
                                    <report-readers-list :report=report></report-readers-list>
                                </div>
                            </#if>
                            <#if isRunner || !fineGrainedAuth>
                                <div id="runReportVueApp" class="mt-5">
                                    <run-report-inline :report=report></run-report-inline>
                                </div>
                            </#if>
                        </div>
                    </div>
                </nav>
            </div>
        </div>
        <div class="col-12 col-md-8 tab-content">
            <div class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="report-tab">
                <#include "partials/report-tab.ftl">
            </div>
            <div class="tab-pane pt-4 pt-md-1" role="tabpanel" id="downloads-tab">
                <#include "partials/downloads.ftl">
            </div>
            <div class="tab-pane pt-4 pt-md-1" role="tabpanel" id="changelog-tab">
                <#include "partials/changelog.ftl">
            </div>
        </div>
    </div>
    <#macro scripts>
        <script type="text/javascript">
            var report = ${reportJson};
        </script>

        <script type="text/javascript" src="${appUrl}/js/report.bundle.js"></script>
    </#macro>
</@layoutwide>
