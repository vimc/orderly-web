package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerClient
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.*
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
import org.vaccineimpact.orderlyweb.models.ReportStatus
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
        OrderlyServerClient(AppConfig()).throwOnError()
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
                    mapOf("output" to "true")
            )
            val latestStatus = statusResponse.data(ReportStatus::class.java)
            updateReportRun(latestStatus, reportRunLogRepository, key)
            log = reportRunLogRepository.getReportRun(key)
        }
        return log
    }

    private fun updateReportRun(latestStatus: ReportStatus, reportRunLogRepository: ReportRunLogRepository, key: String)
    {
        val startDateTime = if (latestStatus.startTime != null)
        {
            Instant.ofEpochSecond(latestStatus.startTime)
        } else
        {
            null
        }

        reportRunLogRepository.updateReportRun(
            key,
            latestStatus.status,
            latestStatus.version,
            latestStatus.output,
            startDateTime
        )
    }

    fun runningReports(): List<ReportRunWithDate>
    {
        val user = context.userProfile!!.id
        return reportRunRepository.getAllReportRunsForUser(user)
    }
}
