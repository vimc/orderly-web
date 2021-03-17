package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.models.ReportRunLog

class ReportRunController(
    context: ActionContext,
    val orderly: OrderlyClient,
    private val reportRepository: ReportRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            Orderly(context),
            OrderlyReportRepository(context)
    )

    fun getRunningReportLogs(): ReportRunLog
    {
        val key = context.params(":key")
        return reportRepository.getReportRun(key)
    }
}
