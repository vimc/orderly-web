package org.vaccineimpact.orderlyweb.viewmodels.vuex

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class RunReportViewModel(val appViewModel: AppViewModel) : AppViewModel by appViewModel
{
    constructor(context: ActionContext) : this(DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Run a report", "${AppConfig()["app.url"]}/vuex-run-report")
    }
}