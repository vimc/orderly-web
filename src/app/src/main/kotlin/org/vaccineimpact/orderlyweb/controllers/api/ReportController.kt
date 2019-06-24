package org.vaccineimpact.orderlyweb.controllers.api

import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.encompass
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError

class ReportController(context: ActionContext,
                       private val orderly: OrderlyClient,
                       private val zip: ZipClient,
                       private val orderlyServerAPI: OrderlyServerAPI,
                       private val config: Config) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    Orderly(context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))),
                    Zip(),
                    OrderlyServer(AppConfig(), KHttpClient()),
                    AppConfig())

    fun run(): String
    {
        val name = context.params(":name")
        val response = orderlyServerAPI.post("/reports/$name/run/", context)
        LoggerFactory.getLogger("TEST").info("--------------- tried to run report ---------------")
        LoggerFactory.getLogger("TEST").info(response.text)
        return passThroughResponse(response)
    }

    fun publish(): String
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val response = orderlyServerAPI.post("/reports/$name/$version/publish/", context)
        LoggerFactory.getLogger("TEST").info("--------------- tried to get report status ---------------")
        LoggerFactory.getLogger("TEST").info(response.text)
         return passThroughResponse(response)
    }


    fun status(): String
    {
        val key = context.params(":key")
        val response = orderlyServerAPI.get("/reports/$key/status/", context)
        return passThroughResponse(response)
    }

    fun getAllReports(): List<Report>
    {
        if (!reportReadingScopes.any())
        {
            throw MissingRequiredPermissionError(PermissionSet("*/reports.read"))
        }

        val reports = orderly.getAllReports()
        return reports.filter { reportReadingScopes.encompass(Scope.Specific("report", it.name)) }
    }

    fun getAllVersions(): List<ReportVersion>
    {
        if (!reportReadingScopes.any())
        {
            throw MissingRequiredPermissionError(PermissionSet("*/reports.read"))
        }

        val reports = orderly.getAllReportVersions()
        return reports.filter { reportReadingScopes.encompass(Scope.Specific("report", it.name)) }
    }

    fun getVersionsByName(): List<String>
    {
        val name = context.params(":name")
        return orderly.getReportsByName(name)
    }


    fun getLatestChangelogByName(): List<Changelog>
    {
        val name = context.params(":name")
        return orderly.getLatestChangelogByName(name)
    }

}
