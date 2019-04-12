package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel

class ReportController(actionContext: ActionContext,
                       private val orderly: OrderlyClient) : Controller(actionContext)
{
    constructor(actionContext: ActionContext) : this(actionContext, Orderly())

    class ReportViewModel(@Serialise("reportJson") val report: ReportVersionDetails,
                          context: ActionContext) : AppViewModel(context)

    @Template("report.ftl")
    fun get(): ReportViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        return ReportViewModel(orderly.getDetailsByNameAndVersion(reportName, version), context)
    }
}