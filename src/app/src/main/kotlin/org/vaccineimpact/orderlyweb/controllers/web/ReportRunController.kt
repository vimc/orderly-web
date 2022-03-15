package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.*
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.models.ReportStatus
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
import java.time.Instant

class ReportRunController(
    context: ActionContext,
    private val reportRunRepository: ReportRunRepository,
    private val workflowRunReportRepository: WorkflowRunReportRepository,
    private val orderlyServerAPI: OrderlyServerAPI
) : Controller(context)
{
    constructor(context: ActionContext) : this(
        context,
        OrderlyWebReportRunRepository(),
        OrderlyWebWorkflowRunReportRepository(),
        OrderlyServer(AppConfig()).throwOnError()
    )

    fun getRunningReportLogs(): ReportRunLog
    {
        val key = context.params(":key")
        val workflow = context.queryParams("workflow")

        if (workflow != null)
        {
            this.workflowRunReportRepository.checkReportIsInWorkflow(key, workflow)
        }

        val reportRunLogRepository: ReportRunLogRepository = if (workflow == null)
        {
            reportRunRepository
        }
        else
        {
           workflowRunReportRepository
        }

        var log = reportRunLogRepository.getReportRun(key)
        if (log.status in listOf(null, "queued", "running"))
        {
            val statusResponse = orderlyServerAPI.get(
                    "/v1/reports/$key/status/",
                    mapOf("output" to "true"))
            val latestStatus = statusResponse.data(ReportStatus::class.java)
            val startDateTime =
                if (latestStatus.startTime != null) Instant.ofEpochSecond(latestStatus.startTime) else Instant.now()
            reportRunLogRepository.updateReportRun(
                key,
                latestStatus.status,
                latestStatus.version,
                latestStatus.output,
                startDateTime)
            log = reportRunLogRepository.getReportRun(key)
        }
        return log
    }

    fun runningReports(): List<ReportRunWithDate>
    {
        val user = context.userProfile!!.id
        return reportRunRepository.getAllReportRunsForUser(user)
    }
}
