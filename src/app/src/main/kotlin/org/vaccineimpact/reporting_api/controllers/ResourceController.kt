package org.vaccineimpact.reporting_api.controllers

import com.google.gson.JsonObject
import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.OrderlyFileNotFoundError
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import javax.servlet.http.HttpServletResponse

class ResourceController(orderlyClient: OrderlyClient? = null, fileServer: FileSystem? = null)  : Controller
{
    val orderly = orderlyClient?: Orderly()
    val files = fileServer?: Files()

    fun get(context: ActionContext): JsonObject {
        return orderly.getResources(context.params(":name"), context.params(":version"))
    }

    fun download(context: ActionContext) : HttpServletResponse {

        val name = context.params(":name")
        val version = context.params(":version")
        val resourcename = context.params(":resource")

        if (!orderly.hasResource(name, version, resourcename))
            throw UnknownObjectError(resourcename, "Resource")

        val filename =  "$name/$version/$resourcename"

        val response = context.getSparkResponse().raw()
        response.setHeader("Content-Disposition", "attachment; filename=$filename")

        val absoluteFilePath = "${Config["orderly.root"]}archive/$filename"

        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(resourcename)

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return response
    }

}
