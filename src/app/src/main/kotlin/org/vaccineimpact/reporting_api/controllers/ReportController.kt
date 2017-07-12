package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.Config
import javax.servlet.http.HttpServletResponse

class ReportController(orderlyClient: OrderlyClient? = null, zipClient: ZipClient? = null) : Controller {

    val orderly = orderlyClient ?: Orderly()
    val zip = zipClient?: Zip()

    fun getAllNames(context: ActionContext): List<String> {
        return orderly.getAllReports()
    }

    fun getVersionsByName(context: ActionContext): List<String> {
        return orderly.getReportsByName(context.params(":name"))
    }

    fun getByNameAndVersion(context: ActionContext): OrderlyReport {
        return orderly.getReportsByNameAndVersion(context.params(":name"), context.params(":version"))
    }

    fun getZippedByNameAndVersion(context: ActionContext): HttpServletResponse {

        val name = context.params(":name")
        val version = context.params(":version")
        val response = context.getSparkResponse()

        response.raw().contentType = "application/zip"
        response.raw().setHeader("Content-Disposition", "attachment; filename=$name/$version.zip")

        val folderName = "${Config["orderly.root"]}archive/$name/$version/"

        zip.zipIt(folderName, response.raw().outputStream)

        // TODO is this needed?
        return response.raw()
    }

}
