package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.encodeFilename
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails

class ReportFileViewModelBuilder(private val report: ReportVersionDetails)
{
    private var inline: Boolean = false

    val baseUrl = "/reports/${report.name}/versions/${report.id}/"

    fun buildArtefactFileViewModel(fileName: String): DownloadableFileViewModel
    {
        val encodedFileName = encodeFilename(fileName)
        return DownloadableFileViewModel(fileName,
                "${baseUrl}artefacts/$encodedFileName?inline=$inline")
    }

    fun buildResourceFileViewModel(fileName: String): DownloadableFileViewModel
    {
        val encodedFileName = encodeFilename(fileName)
        return DownloadableFileViewModel(fileName,
                "${baseUrl}resources/$encodedFileName")
    }

    fun buildZipFileViewModel(): DownloadableFileViewModel
    {
        return DownloadableFileViewModel("${report.name}-${report.id}.zip", "${baseUrl}all/")
    }

    fun buildDataFileViewModel(fileName: String, type: String): DownloadableFileViewModel
    {
        val encodedFileName = encodeFilename(fileName)
        return DownloadableFileViewModel(type, "${baseUrl}data/$encodedFileName/?type=$type")
    }

    fun inline(): ReportFileViewModelBuilder
    {
        this.inline = true
        return this
    }
}
