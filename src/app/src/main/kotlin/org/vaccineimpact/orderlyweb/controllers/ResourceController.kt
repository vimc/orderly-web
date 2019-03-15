package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError

class ResourceController(context: ActionContext,
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
        return orderly.getResourceHashes(context.params(":name"), context.params(":version"))
    }

    fun download(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val resourcename = parseRouteParamToFilepath(context.params(":resource"))

        orderly.getResourceHash(name, version, resourcename)

        val filename = "$name/$version/$resourcename"

        val absoluteFilePath = "${this.config["orderly.root"]}archive/$filename"

        if (!files.fileExists(absoluteFilePath))
            throw OrderlyFileNotFoundError(resourcename)

        context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")
        context.addDefaultResponseHeaders(ContentTypes.binarydata)

        val response = context.getSparkResponse().raw()
        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }

}
