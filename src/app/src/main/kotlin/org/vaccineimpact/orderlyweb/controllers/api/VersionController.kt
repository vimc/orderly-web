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
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.ReportVersionTags
import org.vaccineimpact.orderlyweb.models.ReportVersionWithArtefactsDataDescParamsResources
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.io.File

class VersionController(
        context: ActionContext,
        private val orderly: OrderlyClient,
        private val reportRepository: ReportRepository,
        private val artefactRepository: ArtefactRepository,
        private val zip: ZipClient,
        private val files: FileSystem = Files(),
        private val config: Config
) : Controller(context)
{

    constructor(context: ActionContext) : this(
            context,
            Orderly(context),
            OrderlyReportRepository(context),
            OrderlyArtefactRepository(),
            Zip(),
            Files(),
            AppConfig()
    )

    fun getChangelogByNameAndVersion(): List<Changelog>
    {
        val name = context.params(":name")
        val version = context.params(":version")
        return orderly.getChangelogByNameAndVersion(name, version)
    }

    fun getByNameAndVersion(): ReportVersionWithArtefactsDataDescParamsResources
    {
        val name = context.params(":name")
        return orderly.getDetailsByNameAndVersion(name, context.params(":version"))
    }

    fun getRunMetadata(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")
        reportRepository.getReportVersion(name, version)
        val absoluteFilePath = "${this.config["orderly.root"]}archive/$name/$version/orderly_run.rds"
        return downloadFile(files, absoluteFilePath, "\"$name/$version/orderly_run.rds\"", ContentTypes.binarydata)
    }

    fun getZippedByNameAndVersion(): Boolean
    {
        val name = context.params(":name")
        val version = context.params(":version")

        // check that the requested version exists for the given report
        reportRepository.getReportVersion(name, version)

        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(ContentTypes.zip)
        context.addResponseHeader("Content-Disposition", "attachment; filename=$name/$version.zip")

        val folderName = File("${this.config["orderly.root"]}archive/$name/$version/").absolutePath

        zip.zipIt(folderName, response.outputStream, buildFileList(name, version, folderName))

        return true
    }

    fun getTags(): ReportVersionTags
    {
        val name = context.params(":name")
        val version = context.params(":version")
        return orderly.getReportVersionTags(name, version)
    }

    private fun buildFileList(report: String, version: String, folderName: String): List<String>
    {
        return if (context.hasPermission(ReifiedPermission("reports.review", Scope.Global())))
        {
            files.getAllFilesInFolder(folderName)
        }
        else
        {
            (
                    artefactRepository.getArtefactHashes(report, version) +
                            orderly.getResourceHashes(report, version) +
                            orderly.getReadme(report, version)
            )
            .map { "$folderName/${it.key}" }
        }
    }
}
