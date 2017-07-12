package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import javax.servlet.http.HttpServletResponse

class ArtefactController(orderlyClient: OrderlyClient? = null, fileServer: FileSystem? = null)  : Controller
{
    val orderly = orderlyClient?: Orderly()
    val files = fileServer?: Files()

    fun get(context: ActionContext): Map<String, String> {
        return orderly.getArtefacts(context.params(":name"), context.params(":version"))
    }

    fun download(context: ActionContext) : HttpServletResponse {

        val name = context.params(":name")
        val version = context.params(":version")
        val artefactname = context.params(":artefact")

        if (!orderly.hasArtefact(name, version, artefactname))
            throw UnknownObjectError(artefactname, "Artefact")

        val filename =  "$name/$version/$artefactname"

        val response = context.getSparkResponse().raw()
        response.setHeader("Content-Disposition", "attachment; filename=$filename")

        val absoluteFilePath = "${Config["orderly.root"]}archive/$filename"

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return response
    }

}
