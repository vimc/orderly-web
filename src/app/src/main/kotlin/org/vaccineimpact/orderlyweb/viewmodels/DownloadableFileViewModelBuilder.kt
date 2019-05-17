package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.encodeFilename
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails

class DownloadableFileViewModelBuilder
{
    private var report: ReportVersionDetails? = null
    private var fileName: String = ""
    private var inline: Boolean = false

    private fun baseUrl(): String
    {
        if (report == null || fileName.isEmpty())
        {
            throw IllegalStateException("You must specify a report and a filename before calling this method")
        }
        return "/reports/${report!!.name}/versions/${report!!.id}/"
    }

    fun buildArtefactFileViewModel(): DownloadableFileViewModel
    {
        return DownloadableFileViewModel(fileName, "${baseUrl()}/artefacts/$fileName?inline=$inline")
    }

    fun buildResourceFileViewModel(): DownloadableFileViewModel
    {
        return DownloadableFileViewModel(fileName, "${baseUrl()}/resources/$fileName?inline=$inline")
    }

    fun buildZipFileViewModel(): DownloadableFileViewModel
    {
        return DownloadableFileViewModel(fileName, "${baseUrl()}/all/")
    }

    fun buildDataFileViewModel(type: String): DownloadableFileViewModel
    {
        return DownloadableFileViewModel(fileName, "${baseUrl()}/data/$fileName?type=$type")
    }

    fun withReport(report: ReportVersionDetails): DownloadableFileViewModelBuilder
    {
        this.report = report
        return this
    }

    fun inline(): DownloadableFileViewModelBuilder
    {
        this.inline = true
        return this
    }

    fun withFileName(fileName: String): DownloadableFileViewModelBuilder
    {
        this.fileName = encodeFilename(fileName)
        return this
    }
}
