package org.vaccineimpact.orderlyweb.controllers.web

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.models.ReportRunLogCompanion

class ReportRunController(
    context: ActionContext,
    val orderly: OrderlyClient,
    private val orderlyServerAPI: OrderlyServerAPI,
    private val reportRepository: ReportRepository,
    private val tagRepository: TagRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            Orderly(context),
            OrderlyServer(AppConfig()),
            OrderlyReportRepository(context),
            OrderlyWebTagRepository()
    )

    fun getRunningReportLogs(): ReportRunLog
    {
        val key = context.params(":key")
        return reportRepository.getReportRun(key)
    }
}
