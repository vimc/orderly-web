package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError

class DataController(context: ActionContext,
                     private val orderly: OrderlyClient,
                     private val files: FileSystem,
                     private val config: Config) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    Orderly(context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))),
                    Files(),
                    AppConfig())

    fun get(): Map<String, String>
    {
        return orderly.getData(context.params(":name"), context.params(":version"))
    }

    fun downloadCSV(): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${this.config["orderly.root"]}data/csv/$id.csv"

        return downloadFile(absoluteFilePath, "$id.csv", ContentTypes.csv)
    }

    fun downloadRDS(): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${this.config["orderly.root"]}data/rds/$id.rds"

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

        val absoluteFilePath = "${this.config["orderly.root"]}data/$type/$hash.$type"

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
