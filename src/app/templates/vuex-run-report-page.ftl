<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/report-page.min.css"/>
    </#macro>
    <div id="runReportTabsVueApp">
        <run-report-tabs></run-report-tabs>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/runReportPage-vuex.bundle.js"></script>
    </#macro>
</@layoutwide>
