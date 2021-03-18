package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.models.RunningReports

class LogsController(
    context: ActionContext,
    private val reportRunRepository: ReportRunRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            OrderlyWebReportRunRepository()
    )

    fun runningReports(): List<RunningReports>
    {
        val user = context.userProfile!!.id
        return reportRunRepository.getAllRunningReports(user)
    }
}
