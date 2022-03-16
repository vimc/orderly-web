package org.vaccineimpact.orderlyweb.controllers.web.vuex

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.web.Template
import org.vaccineimpact.orderlyweb.viewmodels.vuex.RunReportViewModel

class ReportController(context: ActionContext) : Controller(context)
{
    @Template("vuex-run-report-page.ftl")
    fun getRunReport(): RunReportViewModel
    {
        return RunReportViewModel(context)
    }
}
