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
        return orderly.getArtefacts(context.params(":name"), context.params(":version"))
    }

    fun download(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val artefactname = parseRouteParamToFilepath(context.params(":artefact"))
        val inline = context.queryParams("inline")?.toBoolean() ?: false

        orderly.getArtefact(name, version, artefactname)

        val filename = "$name/$version/$artefactname"

        val absoluteFilePath = "${this.config["orderly.root"]}archive/$filename"

        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(artefactname)

        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(ContentTypes.binarydata)
        if (!inline)
        {
            context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")
        }

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }

}
