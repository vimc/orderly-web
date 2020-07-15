package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig

data class PublishReportsViewModel(
        @Serialise("reportsWithDraftsJson") val reportsWithDrafts: List<ReportWithDraftsViewModel>,
        val appViewModel: AppViewModel)
    : AppViewModel by appViewModel
{
    constructor(context: ActionContext,
                reportsWithDrafts: List<ReportWithDraftsViewModel>)
            : this(reportsWithDrafts, DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Publish reports", "${AppConfig()["app.url"]}/publish-reports")
    }
}
