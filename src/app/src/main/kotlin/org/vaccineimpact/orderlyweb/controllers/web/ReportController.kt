package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel
import java.net.URLEncoder

class ReportController(actionContext: ActionContext,
                       private val orderly: OrderlyClient) : Controller(actionContext)
{
    constructor(actionContext: ActionContext) : this(actionContext, Orderly())

    open class ReportViewModel(@Serialise("reportJson") open val report: ReportVersionDetails,
                          open val focalArtefactUrl: String?,
                          open val artefacts: List<ArtefactViewModel>,
                          open val dataLinks: List<InputDataViewModel>,
                          open val resources: List<DownloadableFileViewModel>,
                          open val zipFile: DownloadableFileViewModel,
                          context: ActionContext) : AppViewModel(context)

    class ArtefactViewModel(val artefact: Artefact, val files: List<DownloadableFileViewModel>, val inlineArtefactFigure: String?)

    class InputDataViewModel(val key: String, val csv: DownloadableFileViewModel, val rds: DownloadableFileViewModel)

    class DownloadableFileViewModel(val name: String, val url: String)

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        val reportDetails = orderly.getDetailsByNameAndVersion(reportName, version)
        val displayName = reportDetails.displayName ?: reportDetails.name

        val focalArtefact = reportDetails.artefacts.firstOrNull {
            it.files.any { f -> canRenderInBrowser(f) }
        }?.files?.firstOrNull { canRenderInBrowser(it) }

        val focalArtefactUrl = if (focalArtefact == null)
        {
            null
        }
        else
        {
            buildArtefactFileUrl(reportName, version, focalArtefact, true)
        }

        val artefacts = reportDetails.artefacts.map{ ArtefactViewModel(it,
                it.files.map{filename -> DownloadableFileViewModel(filename,
                        buildArtefactFileUrl(reportName, version, filename, false)) },
                getArtefactInlineFigure(reportName, version, it.files))}

        val dataLinks = reportDetails.dataHashes.map{ InputDataViewModel(
                it.key,
                DownloadableFileViewModel("csv", buildDataFileUrl("csv", it.value)),
                DownloadableFileViewModel("rds", buildDataFileUrl("rds", it.value))
        ) }

        val resources = reportDetails.resources.map{DownloadableFileViewModel(it,
                buildResourceUrl(reportName, version, it))}

        val zipFile = DownloadableFileViewModel("$reportName-$version.zip", buildZipFileUrl(reportName, version))

        return ReportViewModel(reportDetails.copy(displayName = displayName),
                focalArtefactUrl,
                artefacts,
                dataLinks,
                resources,
                zipFile,
                context)
    }

    private fun buildArtefactFileUrl(reportName: String, reportVersion: String, fileName: String, inline: Boolean) : String
    {
        val inlineParam = if (inline) "?inline=true" else ""
        return "${baseReportUrl(reportName, reportVersion)}artefacts/$fileName$inlineParam"
    }

    private fun buildDataFileUrl(type: String, dataHash: String): String
    {
        val encodedHash = URLEncoder.encode(dataHash, "UTF-8")
        return "/data/$type/$encodedHash"
    }

    private fun buildResourceUrl(reportName:String, reportVersion: String, resourceName: String): String
    {
        val encodedResourceName = URLEncoder.encode(resourceName, "UTF-8")
        return "${baseReportUrl(reportName, reportVersion)}resources/$encodedResourceName"
    }

    private fun buildZipFileUrl(reportName: String, reportVersion: String): String
    {
        return "${baseReportUrl(reportName, reportVersion)}all/"
    }

    private fun baseReportUrl(reportName: String, reportVersion: String): String
    {
        return "/reports/$reportName/versions/$reportVersion/"
    }

    fun canRenderInBrowser(fileName: String): Boolean
    {
        return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf", "html", "htm"))
    }

    private fun isImage(fileName: String): Boolean
    {
        return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf"))
    }

    private fun extensionIsOneOf(fileName: String, extensions: Array<String>): Boolean
    {
        val ext = fileName.toLowerCase().split(".").last()
        return extensions.contains(ext)
    }

    private fun getArtefactInlineFigure(reportName: String, reportVersion: String, files: List<String>): String?
    {
        //reproducing existing reportle behaviour - show the first file inline if it is an image
        return if (files.count() > 0 && isImage(files[0]))
        {
            buildArtefactFileUrl(reportName, reportVersion, files[0], true)
        }
        else
        {
            null
        }
    }
}