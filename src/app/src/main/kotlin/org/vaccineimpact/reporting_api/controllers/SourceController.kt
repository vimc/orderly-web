package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.FileSystem
import org.vaccineimpact.reporting_api.Files
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import java.io.File
import javax.servlet.http.HttpServletResponse

class SourceController(files: FileSystem? = null): Controller
{
    val files = files?: Files()

    fun downloadCSV(context:ActionContext): HttpServletResponse
    {
        val id = context.params(":id")

        val absoluteFilePath = "${Config["orderly.root"]}data/csv/$id"

        if (!File(absoluteFilePath).exists())
            throw UnknownObjectError(id, "data source")

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
            throw UnknownObjectError(id, "data source")

        val response = context.getSparkResponse().raw()
        response.setHeader("Content-Disposition", "attachment; filename=$id")

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return response
    }

}
