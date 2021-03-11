<#-- @ftlvariable name="runReportMetadataJson" type="String" -->
<#-- @ftlvariable name="gitBranchesJson" type="String" -->
<@layoutwide>
    <#macro styles>
        <link rel="stylesheet" href="${appUrl}/css/report-page.min.css"/>
    </#macro>
    <#--  <div class="row">  -->
        <#--  <div class="col-12 col-md-4 col-xl-3">
            <div class="sidebar pb-4 pb-md-0">
                <nav class="pl-0 pr-0 pr-md-4 navbar navbar-light">
                    <button type="button" class="d-md-none navbar-toggler" data-toggle="collapse"
                            data-target="#sidebar">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="d-md-block mt-4 mt-md-0 collapse navbar-collapse" id="sidebar">
                        <ul class="nav flex-column list-unstyled mb-0">
                            <li class="nav-item">
                                <a id="run-link" class="nav-link active" data-toggle="tab" href="#run-tab" role="tab">Run a report</a>
                            </li>
                            <li class="nav-item">
                                <a id="logs-link" class="nav-link" data-toggle="tab" href="#logs-tab" role="tab">Report logs</a>
                            </li>
                        </ul>
                    </div>
                </nav>
            </div>
        </div>
        <div class="col-12 col-md-8 tab-content">
            <div class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="run-tab">  -->
                <#--  <h2>Run a report</h2>
                <div id="runReportVueApp">
                    <run-report :metadata="runReportMetadata" :initial-git-branches="gitBranches"></run-report>
                </div>  -->
                <#--  <h2>Test header</h2>  -->
                <div id="runReportTabsVueApp">
                    <run-report-tabs :metadata="runReportMetadata" :initial-git-branches="gitBranches"></run-report-tabs>
                </div>
            <#--  </div>  -->
            <#--  <div class="tab-pane pt-4 pt-md-1" role="tabpanel" id="logs-tab">
                <h2>Report logs</h2>
                <p>Report logs coming soon!</p>
            </div>  -->
        <#--  </div>  -->
    </div>
    <#macro scripts>
        <script type="text/javascript">
            var runReportMetadata = ${runReportMetadataJson};
            var gitBranches = ${gitBranchesJson};
        </script>
        <script type="text/javascript" src="${appUrl}/js/runReportTabs.bundle.js"></script>
    </#macro>
</@layoutwide>
