package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.encompass
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError

class ReportController(context: ActionContext,
                       private val reportRepository: ReportRepository,
                       private val reportLogic: ReportLogic,
                       private val orderly: OrderlyClient,
                       private val orderlyServerAPI: OrderlyServerAPI,
                       config: Config) : Controller(context, config)
{
    constructor(context: ActionContext) :
            this(context,
                    OrderlyReportRepository(context),
                    OrderlyReportLogic(context),
                    Orderly(context),
                    OrderlyServer(AppConfig(), KHttpClient()),
                    AppConfig())

    fun run(): String
    {
        val name = context.params(":name")
        val response = orderlyServerAPI.post("/reports/$name/run/", context)
        return passThroughResponse(response)
    }

    fun publish(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        return reportRepository.togglePublishStatus(name, version)
    }

    fun status(): String
    {
        val key = context.params(":key")
        val response = orderlyServerAPI.get("/reports/$key/status/", context)
        return passThroughResponse(response)
    }

    fun getAllReports(): List<Report>
    {
        if (!canReadReports())
        {
            throw MissingRequiredPermissionError(PermissionSet("*/reports.read"))
        }

        return reportRepository.getAllReports()
    }

    fun getAllVersions(): List<ReportVersion>
    {
        if (!canReadReports())
        {
            throw MissingRequiredPermissionError(PermissionSet("*/reports.read"))
        }

        return reportLogic.getAllReportVersions()
    }

    fun getVersionsByName(): List<String>
    {
        val name = context.params(":name")
        return reportRepository.getReportsByName(name)
    }


    fun getLatestChangelogByName(): List<Changelog>
    {
        val name = context.params(":name")
        return orderly.getLatestChangelogByName(name)
    }

}
