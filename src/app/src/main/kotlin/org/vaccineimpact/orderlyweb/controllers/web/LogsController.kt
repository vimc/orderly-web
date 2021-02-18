package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
// import org.vaccineimpact.orderlyweb.OrderlyServer
// import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
// import org.vaccineimpact.orderlyweb.db.AppConfig
// import org.vaccineimpact.orderlyweb.db.Orderly
// import org.vaccineimpact.orderlyweb.db.OrderlyClient
// import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
// import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
// import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
// import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
// import org.vaccineimpact.orderlyweb.errors.BadRequest
// import org.vaccineimpact.orderlyweb.models.*
// import org.vaccineimpact.orderlyweb.viewmodels.PublishReportsViewModel
// import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel
// import org.vaccineimpact.orderlyweb.viewmodels.ReportWithDraftsViewModel
// import org.vaccineimpact.orderlyweb.viewmodels.RunReportViewModel
import org.vaccineimpact.orderlyweb.models.Running

class LogsController(
    context: ActionContext,
    // val orderly: OrderlyClient,
    // private val orderlyServerAPI: OrderlyServerAPI,
    // private val reportRepository: ReportRepository,
    // private val tagRepository: TagRepository
    private val reportRunRepository: ReportRunRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            // Orderly(context),
            // OrderlyServer(AppConfig()),
            // OrderlyReportRepository(context),
            // OrderlyWebTagRepository()
            OrderlyWebReportRunRepository()
    )


    fun running(): List<Running>
    {
        val user = context.userProfile!!.id
        return reportRunRepository.getAllRunningReports(user)
    }

}
