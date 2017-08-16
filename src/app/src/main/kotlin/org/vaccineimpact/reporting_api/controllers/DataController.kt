package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.FileSystem
import org.vaccineimpact.reporting_api.Files
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.OrderlyFileNotFoundError

class DataController(context: ActionContext,
                     val orderly: OrderlyClient,
                     val files: FileSystem) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context, Orderly(context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))), Files())

    fun get(): JsonObject
    {
        return orderly.getData(context.params(":name"), context.params(":version"))
    }

    fun downloadCSV(): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${Config["orderly.root"]}data/csv/$id.csv"

        return downloadFile(absoluteFilePath, "$id.csv", ContentTypes.csv)
    }

    fun downloadRDS(): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${Config["orderly.root"]}data/rds/$id.rds"

        return downloadFile(absoluteFilePath, "$id.rds", ContentTypes.binarydata)
    }

    fun downloadData(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val id = context.params(":data")
        var type = context.queryParams("type")

        if (type.isNullOrEmpty())
            type = "csv"

        val hash = orderly.getDatum(name, version, id)

        val absoluteFilePath = "${Config["orderly.root"]}data/$type/$hash.$type"

        val contentType =
        if (type == "csv")
        {
            ContentTypes.csv
        }
        else
        {
           ContentTypes.binarydata
        }

        return downloadFile(absoluteFilePath, "$hash.$type", contentType)
    }

    private fun downloadFile(absoluteFilePath: String, filename: String,
                             contentType: String): Boolean
    {
        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(filename)

        val response = context.getSparkResponse().raw()

        context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")
        context.addDefaultResponseHeaders(contentType)

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }
}
