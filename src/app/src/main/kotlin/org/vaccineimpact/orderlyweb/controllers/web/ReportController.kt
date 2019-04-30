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

    class ReportViewModel(@Serialise("reportJson") val report: ReportVersionDetails,
                          val focalArtefactUrl: String?,
                          val artefacts: List<ArtefactViewModel>,
                          val dataLinks: List<InputDataViewModel>,
                          context: ActionContext) : AppViewModel(context)

    class ArtefactViewModel(val artefact: Artefact, val files: List<DownloadableFileViewModel>, val inlineArtefactFigure: String?)

    class InputDataViewModel(val key: String, val csv: DownloadableFileViewModel, val rds: DownloadableFileViewModel)

    class DownloadableFileViewModel(val fileName: String, val url: String)

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

        return ReportViewModel(reportDetails.copy(displayName = displayName), focalArtefactUrl, artefacts,
                dataLinks, context)
    }

    fun buildArtefactFileUrl(reportName: String, reportVersion: String, fileName: String, inline: Boolean) : String
    {
        val inlineParam = if (inline) "?inline=true" else ""
        return "/reports/$reportName/versions/$reportVersion/artefacts/$fileName$inlineParam"
    }

    fun buildDataFileUrl(type: String, dataHash: String): String
    {
        val encodedHash = URLEncoder.encode(dataHash)
        return "/data/$type/$encodedHash"
    }

    fun canRenderInBrowser(fileName: String): Boolean
    {
        return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf", "html", "htm"))
    }

    fun isImage(fileName: String): Boolean
    {
        return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf"))
    }

    fun extensionIsOneOf(fileName: String, extensions: Array<String>): Boolean
    {
        val ext = fileName.toLowerCase().split(".").last()
        return extensions.contains(ext)
    }

    fun getArtefactInlineFigure(reportName: String, reportVersion: String, files: List<String>): String?
    {
        //reproducing existing reportle behaviour, but we may want to extend this
        // show the first file inline if it is an image
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