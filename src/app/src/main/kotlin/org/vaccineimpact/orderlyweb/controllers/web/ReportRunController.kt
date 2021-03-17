package org.vaccineimpact.orderlyweb.controllers.web

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.models.ReportRunLog

class ReportRunController(
    context: ActionContext,
    val orderly: OrderlyClient,
    private val reportRunRepository: ReportRunRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            Orderly(context),
            OrderlyWebReportRunRepository()
    )

    fun getRunningReportLogs(): ReportRunLog
    {
        val key = context.params(":key")
        return reportRunRepository.getReportRun(key)
    }
}
