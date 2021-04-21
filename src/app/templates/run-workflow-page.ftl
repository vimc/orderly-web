
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/report-page.min.css"/>
    </#macro>
    <div id="runWorkflowTabsVueApp">
        <run-workflow-tabs></run-workflow-tabs>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/runWorkflowTabs.bundle.js"></script>
    </#macro>
</@layoutwide>
