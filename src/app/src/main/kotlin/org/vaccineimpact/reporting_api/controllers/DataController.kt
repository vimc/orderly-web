package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.FileSystem
import org.vaccineimpact.reporting_api.Files
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.OrderlyFileNotFoundError
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import java.io.File
import javax.servlet.http.HttpServletResponse

class DataController(orderly: OrderlyClient? = null, files: FileSystem? = null): Controller
{
    val files = files?: Files()
    val orderly = orderly?: Orderly()

    fun downloadCSV(context:ActionContext): HttpServletResponse
    {
        val id = context.params(":id")

        val absoluteFilePath = "${Config["orderly.root"]}data/csv/$id"

        if (!File(absoluteFilePath).exists())
            throw OrderlyFileNotFoundError(id)

        val response = context.getSparkResponse().raw()
        response.setHeader("Content-Disposition", "attachment; filename=$id")

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return response
    }

    fun downloadRDS(context:ActionContext): HttpServletResponse
    {
        val id = context.params(":id")

        val absoluteFilePath = "${Config["orderly.root"]}data/rds/$id"

        if (!File(absoluteFilePath).exists())
            throw OrderlyFileNotFoundError(id)

        val response = context.getSparkResponse().raw()
        response.setHeader("Content-Disposition", "attachment; filename=$id")

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return response
    }

    fun downloadData(context:ActionContext): HttpServletResponse
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val id = context.params(":data")
        var type = context.queryParams(":type")

        if (type.isNullOrEmpty())
            type = "csv"

        if (!orderly.hasData(name, version, id))
            throw UnknownObjectError(id, "Data source")

        val absoluteFilePath = "${Config["orderly.root"]}data/$type/$id.$type"

        if (!File(absoluteFilePath).exists())
            throw OrderlyFileNotFoundError(id)

        val response = context.getSparkResponse().raw()
        response.setHeader("Content-Disposition", "attachment; filename=$id")

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return response
    }

}
