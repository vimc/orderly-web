package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.models.ReportStatus

class ReportRunController(
    context: ActionContext,
    private val reportRunRepository: ReportRunRepository,
    private val orderlyServerAPI: OrderlyServerAPI
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            OrderlyWebReportRunRepository(),
            OrderlyServer(AppConfig()).throwOnError()
    )

    fun getRunningReportLogs(): ReportRunLog
    {
        val key = context.params(":key")
        var log = reportRunRepository.getReportRun(key)
        if (log.status in listOf(null, "queued", "running"))
        {
            val statusResponse = orderlyServerAPI.get(
                    "/v1/reports/$key/status/",
                    mapOf("output" to "true"))
            val latestStatus = statusResponse.data(ReportStatus::class.java)
            reportRunRepository.updateReportRun(key, latestStatus.status, latestStatus.version, latestStatus.output)
            log = reportRunRepository.getReportRun(key)
        }
        return log
    }
}
