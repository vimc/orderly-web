package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ArtefactRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyArtefactRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError

class ArtefactController(context: ActionContext,
                         private val reportRepository: ReportRepository,
                         private val artefactRepository: ArtefactRepository,
                         private val files: FileSystem,
                         private val config: Config)

    : Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    OrderlyReportRepository(context),
                    OrderlyArtefactRepository(),
                    Files(),
                    AppConfig())

    fun getMetaData(): Map<String, String>
    {
        val name = context.params(":name")
        val version = context.params(":version")
        reportRepository.getReportVersion(name, version)
        return artefactRepository.getArtefactHashes(name, version)
    }

    fun getFile(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        val artefactname = parseRouteParamToFilepath(context.params(":artefact"))
        val inline = context.queryParams("inline")?.toBoolean() ?: false

        reportRepository.getReportVersion(name, version)
        artefactRepository.getArtefactHash(name, version, artefactname)

        val filename = "$name/$version/$artefactname"

        val absoluteFilePath = "${this.config["orderly.root"]}archive/$filename"

        if (!files.fileExists(absoluteFilePath))
        {
            throw OrderlyFileNotFoundError(artefactname)
        }

        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(guessFileType(filename))
        if (!inline)
        {
            context.addResponseHeader("Content-Disposition", "attachment; filename=\"$filename\"")
        }

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }

}
