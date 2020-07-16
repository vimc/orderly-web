<@layout>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/publish-reports.min.css"/>
    </#macro>
    <div id="publishReportsApp">
        <publish-reports :reports-with-drafts="reportsWithDrafts"></publish-reports>
    </div>
    <#macro scripts>
        <script type="text/javascript">
            var reportsWithDrafts = ${reportsWithDraftsJson};
        </script>
        <script type="text/javascript" src="${appUrl}/js/publishReports.bundle.js"></script>
    </#macro>
</@layout>
