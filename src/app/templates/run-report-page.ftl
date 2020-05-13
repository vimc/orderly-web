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
                                <a class="nav-link active" data-toggle="tab" href="#run-tab" role="tab">Run a report</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab" href="#logs-tab" role="tab">Report logs</a>
                            </li>
                        </ul>
                    </div>
                </nav>
            </div>
        </div>
        <div class="col-12 col-md-8 tab-content">
            <div class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="run-tab">
                <h2>Run a report</h2>
                <div id="runReportVueApp">
                    <run-report></run-report>
                </div>
            </div>
            <div class="tab-pane pt-4 pt-md-1" role="tabpanel" id="logs-tab">
               <h2>Report logs</h2>
                <p>Report logs coming soon!</p>
            </div>
        </div>
    </div>
    <#macro scripts>
        <script type="text/javascript" src="${appUrl}/js/runReport.bundle.js"></script>
        <script type="text/javascript" src="${appUrl}/js/lib/bootstrap.min.js"></script>
    </#macro>
</@layoutwide>
