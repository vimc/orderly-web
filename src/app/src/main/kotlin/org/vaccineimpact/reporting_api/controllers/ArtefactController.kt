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

class ArtefactController(orderlyClient: OrderlyClient? = null, fileServer: FileSystem? = null) : Controller
{
    val orderly = orderlyClient ?: Orderly()
    val files = fileServer ?: Files()

    fun get(context: ActionContext): JsonObject
    {
        return orderly.getArtefacts(context.params(":name"), context.params(":version"))
    }

    fun download(context: ActionContext): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val artefactname = context.params(":artefact")

        orderly.getArtefact(name, version, artefactname)

        val filename = "$name/$version/$artefactname"

        val absoluteFilePath = "${Config["orderly.root"]}archive/$filename"

        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(artefactname)

        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(ContentTypes.binarydata)
        context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }

}
