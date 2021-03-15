<#-- @ftlvariable name="runReportMetadataJson" type="String" -->
<#-- @ftlvariable name="gitBranchesJson" type="String" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/report-page.min.css"/>
    </#macro>
    <div id="runReportTabsVueApp">
        <run-report-tabs :metadata="runReportMetadata" :initial-git-branches="gitBranches"></run-report-tabs>
    </div>
    <#macro scripts>
        <script type="text/javascript">
            var runReportMetadata = ${runReportMetadataJson};
            var gitBranches = ${gitBranchesJson};
        </script>
        <script type="text/javascript" src="${appUrl}/js/runReportTabs.bundle.js"></script>
    </#macro>
</@layoutwide>
