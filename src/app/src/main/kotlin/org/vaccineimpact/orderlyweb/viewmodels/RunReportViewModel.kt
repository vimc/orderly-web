package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig

data class RunReportViewModel(val appViewModel: AppViewModel)
    : AppViewModel by appViewModel
{
    constructor(context: ActionContext)
            : this(DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Run a report", "${AppConfig()["app.url"]}/run-report")
    }
}