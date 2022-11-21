<#-- @ftlvariable name="runReportMetadataJson" type="String" -->
<#-- @ftlvariable name="gitBranchesJson" type="String" -->
<#-- @ftlvariable name="initialReportName" type="String" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/report-page.css"/>
    </#macro>
        <div id="runReportTabsVueApp">
            <run-report-tabs :initial-report-name="initialReportName"></run-report-tabs>
        </div>
    <#macro scripts>
        <script type="text/javascript">
            var initialReportName = ${initialReportName};
        </script>
        <script type="text/javascript" src="${appUrl}/js/runReportTabs.bundle.js"></script>
    </#macro>
</@layoutwide>
