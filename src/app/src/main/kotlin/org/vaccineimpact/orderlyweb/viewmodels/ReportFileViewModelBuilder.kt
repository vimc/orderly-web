package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.encodeFilename

class ReportFileViewModelBuilder(private val reportName: String,
                                 private val reportVersion: String,
                                 appConfig: Config = AppConfig())
{
    private var inline: Boolean = false

    val baseUrl = "${appConfig["app.url"]}/reports/${reportName}/versions/${reportVersion}/"

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
        return DownloadableFileViewModel("${reportName}-${reportVersion}.zip", "${baseUrl}all/")
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
