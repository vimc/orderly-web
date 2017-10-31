package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.api.models.Report
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient

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
        return orderly.getAllReports()
    }

    fun getVersionsByName(): List<String>
    {
        return orderly.getReportsByName(context.params(":name"))
    }

    fun getByNameAndVersion(): JsonObject
    {
        return orderly.getReportsByNameAndVersion(context.params(":name"), context.params(":version"))
    }

    fun getZippedByNameAndVersion(): Boolean
    {

        val name = context.params(":name")
        val version = context.params(":version")
        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(ContentTypes.zip)
        context.addResponseHeader("Content-Disposition", "attachment; filename=$name/$version.zip")

        val folderName = "${this.config["orderly.root"]}archive/$name/$version/"

        zip.zipIt(folderName, response.outputStream)

        return true
    }

}
