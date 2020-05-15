package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.RunReportMetadata

data class RunReportViewModel(
        @Serialise("runReportMetadataJson") val runReportMetadata: RunReportMetadata,
        @Serialise("gitBranchesJson") val gitBranches: List<String>?,
        val appViewModel: AppViewModel)
    : AppViewModel by appViewModel
{
    constructor(context: ActionContext, runReportMetadata: RunReportMetadata, gitBranches: List<String>?)
            : this(runReportMetadata, gitBranches, DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Run a report", "${AppConfig()["app.url"]}/run-report")
    }
}