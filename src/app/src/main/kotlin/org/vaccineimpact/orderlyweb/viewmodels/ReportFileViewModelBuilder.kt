package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.encodeFilename
import org.vaccineimpact.orderlyweb.models.FileInfo

class ReportFileViewModelBuilder(
        private val reportName: String,
        private val reportVersion: String,
        appConfig: Config = AppConfig()
)
{
    private var inline: Boolean = false

    val baseUrl = "${appConfig["app.url"]}/report/$reportName/version/$reportVersion/"

    fun buildArtefactFileViewModel(file: FileInfo): DownloadableFileViewModel
    {
        val encodedFileName = encodeFilename(file.name)
        return DownloadableFileViewModel(
                file.name,
                "${baseUrl}artefacts/$encodedFileName?inline=$inline",
                file.size
        )
    }

    fun buildResourceFileViewModel(file: FileInfo): DownloadableFileViewModel
    {
        val encodedFileName = encodeFilename(file.name)
        return DownloadableFileViewModel(
                file.name,
                "${baseUrl}resources/$encodedFileName",
                file.size
        )
    }

    fun buildZipFileViewModel(): DownloadableFileViewModel
    {
        return DownloadableFileViewModel("$reportName-$reportVersion.zip", "${baseUrl}all/", null)
    }

    fun buildDataFileViewModel(fileName: String, type: String, size: Long): DownloadableFileViewModel
    {
        val encodedFileName = encodeFilename(fileName)
        return DownloadableFileViewModel(type, "${baseUrl}data/$encodedFileName/?type=$type", size)
    }

    fun inline(): ReportFileViewModelBuilder
    {
        this.inline = true
        return this
    }
}
