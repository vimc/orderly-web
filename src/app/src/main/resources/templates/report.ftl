<#-- @ftlvariable name="report" type="org.vaccineimpact.orderlyweb.models.Report" -->
<#-- @ftlvariable name="reportDetailsJson" type="String" -->
<@layoutwide>
    <#macro styles>
    <link rel="stylesheet" href="/css/report-page.min.css"/>
    </#macro>
<div class="row" id="vueApp">
    <div class="col-12 col-md-4 col-xl-3 sidebar">
        <ul class="list-unstyled mb-0">
            <li class="nav-item">
                <a href="#" id="report"
                   v-bind:class="['nav-link', {'active': tab ==='report'}]"
                   v-on:click="() => switchTab('report')">Report</a>
            </li>
            <li class="nav-item">
                <a href="#" id="downloads" class="nav-link"
                   v-bind:class="['nav-link', {'active': tab ==='downloads'}]"
                   v-on:click="() => switchTab('downloads')">Downloads</a>
            </li>
        </ul>
        <hr/>
        <publish-switch :report=report @toggle="handleToggle"></publish-switch>
    </div>
    <div class="col-12 col-md-8 col-xl-9">
        <div v-show="tab === 'report'">
          <#include "partials/report.ftl">
        </div>
        <div v-show="tab === 'downloads'">
            <#include "partials/downloads.ftl">
        </div>
    </div>
</div>
    <#macro scripts>
<script>
    var report = ${reportDetailsJson}
</script>
    <script src="/js/report.bundle.js"></script>
    </#macro>
</@layoutwide>
