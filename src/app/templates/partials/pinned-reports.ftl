<#-- @ftlvariable name="pinnedReports" type="List<PinnedReportViewModel>" -->
<#import "../macros/download-icon.ftl" as download_icon>
<#if pinnedReports?has_content>
    <h1 class="h3 mb-3 pinned-reports">Pinned Reports</h1>
    <div id="pinned-reports" class="row mb-5">
        <#list pinnedReports as pinnedReport>
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card">
                    <div class="card-header">
                        <a href="/reports/${pinnedReport.name}/${pinnedReport.version}/">${pinnedReport.displayName}</a>
                        <div class="text-muted small">Updated: ${pinnedReport.date}</div>
                    </div>
                    <div class="card-body">
                        <a class="button pinned-report-link" target="_blank" download="" href="${pinnedReport.zipFile.url}">
                            Download latest
                            <@download_icon.render fill="#fff" />
                        </a>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</#if>