package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.FileSystem
import org.vaccineimpact.reporting_api.Files
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.OrderlyFileNotFoundError

class DataController(orderly: OrderlyClient? = null, files: FileSystem? = null) : Controller
{
    val files = files ?: Files()
    val orderly = orderly ?: Orderly()

    fun get(context: ActionContext): JsonObject
    {
        return orderly.getData(context.params(":name"), context.params(":version"))
    }

    fun downloadCSV(context: ActionContext): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${Config["orderly.root"]}data/csv/$id.csv"

        context.addResponseHeader("Content-Type", ContentTypes.csv)
        return downloadFile(absoluteFilePath, "$id.csv", context)
    }

    fun downloadRDS(context: ActionContext): Boolean
    {
        val id = context.params(":id")
        val absoluteFilePath = "${Config["orderly.root"]}data/rds/$id.rds"

        context.addResponseHeader("Content-Type", ContentTypes.binarydata)
        return downloadFile(absoluteFilePath, "$id.rds", context)
    }

    fun downloadData(context: ActionContext): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val id = context.params(":data")
        var type = context.queryParams("type")

        if (type.isNullOrEmpty())
            type = "csv"

        val hash = orderly.getDatum(name, version, id)

        val absoluteFilePath = "${Config["orderly.root"]}data/$type/$hash.$type"

        if (type == "csv")
        {
            context.addResponseHeader("Content-Type", ContentTypes.csv)
        }
        else
        {
            context.addResponseHeader("Content-Type", ContentTypes.binarydata)
        }

        return downloadFile(absoluteFilePath, "$hash.$type", context)
    }

    private fun downloadFile(absoluteFilePath: String, filename: String, context: ActionContext): Boolean
    {
        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(filename)

        val response = context.getSparkResponse().raw()

        context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }
}
