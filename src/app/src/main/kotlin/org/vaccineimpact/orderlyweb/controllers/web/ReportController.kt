package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.*
import java.net.URLEncoder

class ReportController : OrderlyDataController
{
    constructor(actionContext: ActionContext,
                orderly: OrderlyClient) : super(actionContext, orderly)

    constructor(actionContext: ActionContext) : super(actionContext)

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportVersionPageViewModel
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


        val artefacts = reportDetails.artefacts.map {
            ArtefactViewModel(it,
                    it.files.map { filename ->
                        DownloadableFileViewModel(filename,
                                buildArtefactFileUrl(reportName, version, filename, false))
                    },
                    getArtefactInlineFigure(reportName, version, it.files))
        }

        val dataLinks = reportDetails.dataHashes.map {
            InputDataViewModel(
                    it.key,
                    DownloadableFileViewModel("csv", buildDataFileUrl(reportName, version, "csv", it.key)),
                    DownloadableFileViewModel("rds", buildDataFileUrl(reportName, version, "rds", it.key))
            )
        }

        val resources = reportDetails.resources.map {
            DownloadableFileViewModel(it,
                    buildResourceUrl(reportName, version, it))
        }

        val zipFile = DownloadableFileViewModel("$reportName-$version.zip", buildZipFileUrl(reportName, version))

        val isAdmin = context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))

        return ReportVersionPageViewModel(reportDetails.copy(displayName = displayName),
                focalArtefactUrl,
                isAdmin,
                artefacts,
                dataLinks,
                resources,
                zipFile,
                context)
    }

    private fun buildArtefactFileUrl(reportName: String, reportVersion: String, fileName: String, inline: Boolean): String
    {
        val inlineParam = if (inline) "?inline=true" else ""
        return "${baseReportUrl(reportName, reportVersion)}artefacts/$fileName$inlineParam"
    }

    private fun buildDataFileUrl(reportName: String, reportVersion: String, type: String, dataHash: String): String
    {
        val encodedHash = URLEncoder.encode(dataHash, "UTF-8")
        return "${baseReportUrl(reportName, reportVersion)}data/$encodedHash/?type=$type"
    }

    private fun buildResourceUrl(reportName: String, reportVersion: String, resourceName: String): String
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