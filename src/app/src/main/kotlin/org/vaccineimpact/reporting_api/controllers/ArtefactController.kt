package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.OrderlyFileNotFoundError
import java.io.File

class ArtefactController(context: ActionContext,
                         private val orderly: OrderlyClient,
                         private val files: FileSystem,
                         private val config: Config)

    : Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    Orderly(context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))),
                    Files(),
                    AppConfig())

    fun get(): JsonObject
    {
        return orderly.getArtefactHashes(context.params(":name"), context.params(":version"))
    }

    fun download(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val artefactname = parseRouteParamToFilepath(context.params(":artefact"))
        val inline = context.queryParams("inline")?.toBoolean() ?: false

        orderly.getArtefactHash(name, version, artefactname)

        val filename = "$name/$version/$artefactname"

        val absoluteFilePath = "${this.config["orderly.root"]}archive/$filename"

        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(artefactname)

        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(guessFileType(filename))
        if (!inline)
        {
            context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")
        }

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }

    // TODO: Just return the mime type of the artefact file, once we have that metadata
    private fun guessFileType(filename: String): String
    {
        val ext = File(filename).extension
        return when (ext)
        {
            "csv" -> "text/csv"
            "png" -> "image/png"
            "svg" -> "image/svg+xml"
            "pdf" -> "application/pdf"
            "html" -> "text/html"
            "css" -> "text/css"
            else -> ContentTypes.binarydata
        }
    }

}
