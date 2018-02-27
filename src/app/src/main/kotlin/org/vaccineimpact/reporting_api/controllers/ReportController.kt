package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.api.models.Report
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.encompass
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.api.models.permissions.RoleAssignment
import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.MissingRequiredPermissionError

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
        return passThroughResponse(response)
    }

    fun publish(): String
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val response = orderlyServerAPI.post("/reports/$name/$version/publish/", context)
        return passThroughResponse(response)
    }


    fun status(): String
    {
        val key = context.params(":key")
        val response = orderlyServerAPI.get("/reports/$key/status/", context)
        return passThroughResponse(response)
    }

    fun getAllNames(): List<Report>
    {
        val reports = orderly.getAllReports()
        return reports.filter { reportReadingScopes.encompass(Scope.Specific("report", it.name)) }
    }

    fun getVersionsByName(): List<String>
    {
        val name = authorizedReport()
        return orderly.getReportsByName(name)
    }

    fun getByNameAndVersion(): JsonObject
    {
        val name = authorizedReport()
        return orderly.getReportsByNameAndVersion(name, context.params(":version"))
    }

    fun getZippedByNameAndVersion(): Boolean
    {
        val name = authorizedReport()
        val version = context.params(":version")
        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(ContentTypes.zip)
        context.addResponseHeader("Content-Disposition", "attachment; filename=$name/$version.zip")

        val folderName = "${this.config["orderly.root"]}archive/$name/$version/"

        zip.zipIt(folderName, response.outputStream)

        return true
    }

    private fun authorizedReport(): String {
        val name = context.params(":name")
        val requiredScope = Scope.Specific("report", name)
        if (!reportReadingScopes.any({ it.encompasses(requiredScope) }))
        {
            throw MissingRequiredPermissionError(PermissionSet("$requiredScope/reports.read"))
        }

        return name
    }

    private val reportReadingScopes = context.permissions
            .filter { it.name == "reports.read" }
            .map { it.scope }

}
