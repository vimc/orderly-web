package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError
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

    fun get(): Map<String, String>
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
