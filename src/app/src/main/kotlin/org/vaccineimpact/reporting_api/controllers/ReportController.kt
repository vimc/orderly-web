package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.Zip
import org.vaccineimpact.reporting_api.ZipClient
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient

class ReportController(orderlyClient: OrderlyClient? = null, zipClient: ZipClient? = null) : Controller
{

    val orderly = orderlyClient ?: Orderly()
    val zip = zipClient ?: Zip()

    fun getAllNames(context: ActionContext): List<String>
    {
        return orderly.getAllReports()
    }

    fun getVersionsByName(context: ActionContext): List<String>
    {
        return orderly.getReportsByName(context.params(":name"))
    }

    fun getByNameAndVersion(context: ActionContext): JsonObject
    {
        return orderly.getReportsByNameAndVersion(context.params(":name"), context.params(":version"))
    }

    fun getZippedByNameAndVersion(context: ActionContext): Boolean
    {

        val name = context.params(":name")
        val version = context.params(":version")
        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(ContentTypes.zip)
        context.addResponseHeader("Content-Disposition", "attachment; filename=$name/$version.zip")

        val folderName = "${Config["orderly.root"]}archive/$name/$version/"

        zip.zipIt(folderName, response.outputStream)

        return true
    }

}
