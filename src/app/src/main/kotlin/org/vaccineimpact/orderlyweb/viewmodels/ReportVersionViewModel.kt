package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.isImage
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.apache.commons.io.FileUtils
import org.vaccineimpact.orderlyweb.models.*


data class ReportVersionPageViewModel(@Serialise("reportJson") val report: ReportVersionDetails,
                                      val focalArtefactUrl: String?,
                                      val isRunner: Boolean,
                                      val artefacts: List<ArtefactViewModel>,
                                      val dataLinks: List<InputDataViewModel>,
                                      val resources: List<DownloadableFileViewModel>,
                                      val zipFile: DownloadableFileViewModel,
                                      val versions: List<VersionPickerViewModel>,
                                      val changelog: List<ChangelogViewModel>,
                                      val appViewModel: AppViewModel) :
        AppViewModel by appViewModel
{
    companion object
    {
        fun build(report: ReportVersionDetails,
                  versions: List<String>,
                  changelog: List<Changelog>,
                  context: ActionContext): ReportVersionPageViewModel
        {
            val fileViewModelBuilder = ReportFileViewModelBuilder(report.name, report.id)

            val artefactViewModels = report.artefacts.map {
                buildArtefact(fileViewModelBuilder, it)
            }

            val focalArtefactUrl = getFocalArtefactUrl(fileViewModelBuilder, report.artefacts)

            val dataViewModels = report.dataInfo.map {
                InputDataViewModel(
                        it.name,
                        fileViewModelBuilder.buildDataFileViewModel(it.name, "csv", it.csvSize),
                        fileViewModelBuilder.buildDataFileViewModel(it.name, "rds", it.rdsSize))
            }

            val resourceViewModels = report.resources.map {
                fileViewModelBuilder
                        .buildResourceFileViewModel(it)
            }

            val zipFile = fileViewModelBuilder
                    .buildZipFileViewModel()

            val isRunner = context.hasPermission(ReifiedPermission("reports.run", Scope.Global()))

            val displayName = report.displayName ?: report.name

            val breadcrumb = Breadcrumb("${report.name} (${report.id})", "${AppConfig()["app.url"]}/report/${report.name}/${report.id}/")

            val changelogViewModel = changelog.sortedByDescending { it.reportVersion }
                    .groupBy { it.reportVersion }.map {
                        ChangelogViewModel.build(it.key, it.value)
                    }

            return ReportVersionPageViewModel(report.copy(displayName = displayName),
                    focalArtefactUrl,
                    isRunner,
                    artefactViewModels,
                    dataViewModels,
                    resourceViewModels,
                    zipFile,
                    versions.sortedByDescending { it }.map { buildVersionPickerViewModel(report.name, report.id, it) },
                    changelogViewModel,
                    DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }

        private fun getFocalArtefactUrl(builder: ReportFileViewModelBuilder, artefacts: List<Artefact>): String?
        {
            //reproducing existing reportle behaviour - show the first artefact inline if it is possible
            val focalArtefactFile = if (artefacts.any() && artefacts[0].files.any() && canRenderInBrowser(artefacts[0].files[0].name))
            {
                artefacts[0].files[0]
            }
            else
            {
                null
            }

            return if (focalArtefactFile == null)
            {
                null
            }
            else
            {
                builder
                        .inline()
                        .buildArtefactFileViewModel(focalArtefactFile)
                        .url
            }
        }

        private fun buildArtefact(fileBuilder: ReportFileViewModelBuilder, artefact: Artefact): ArtefactViewModel
        {
            val inlineFile = getInlineFigureFile(artefact.files)
            val inline = if (inlineFile == null)
            {
                null
            }
            else
            {
                fileBuilder
                        .inline()
                        .buildArtefactFileViewModel(inlineFile)
                        .url
            }

            val files = artefact.files.map {
                fileBuilder.buildArtefactFileViewModel(it)
            }

            return ArtefactViewModel(artefact, files, inline)
        }

        private fun getInlineFigureFile(files: List<FileInfo>): FileInfo?
        {
            //reproducing existing reportle behaviour - show the first file inline if it is an image
            return if (files.count() > 0 && isImage(files[0].name))
            {
                files[0]
            }
            else
            {
                null
            }
        }

        private fun buildVersionPickerViewModel(reportName: String, currentVersion: String, id: String): VersionPickerViewModel
        {
            val date = getDateStringFromVersionId(id)
            return VersionPickerViewModel("${AppConfig()["app.url"]}/report/$reportName/$id", date,
                    selected = id == currentVersion)
        }

        fun getDateStringFromVersionId(id: String): String
        {
            val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy, HH:mm")
            val regex = Regex("(\\d{4})(\\d{2})(\\d{2})-(\\d{2})(\\d{2})(\\d{2})-([0-9a-f]{8})")
            val match = regex.matchEntire(id)
                    ?.groupValues ?: throw Exception("Badly formatted report id $id")

            val date = LocalDateTime.parse("${match[1]}-${match[2]}-${match[3]}T${match[4]}:${match[5]}:${match[6]}")
            return formatter.format(date)
        }

    }
}

data class VersionPickerViewModel(val url: String, val date: String, val selected: Boolean)

data class ArtefactViewModel(val artefact: Artefact,
                             val files: List<DownloadableFileViewModel>,
                             val inlineArtefactFigure: String?)

data class InputDataViewModel(val key: String,
                              val csv: DownloadableFileViewModel,
                              val rds: DownloadableFileViewModel)

data class DownloadableFileViewModel(val name: String, val url: String, val size: Long?)
{
    val formattedSize get() =
        if (size != null)
        {
            FileUtils.byteCountToDisplaySize(size)
        }
        else
        {
            null
        }
}

data class ChangelogViewModel(val date: String, val version: String, val entries: List<ChangelogItemViewModel>)
{
    companion object
    {
        fun build(id: String, changelog: List<Changelog>): ChangelogViewModel
        {
            val date = ReportVersionPageViewModel.getDateStringFromVersionId(id)
            val entries = changelog.map { ChangelogItemViewModel(it.label, it.value) }
            return ChangelogViewModel(date, id, entries)
        }
    }
}

data class ChangelogItemViewModel(val label: String, val value: String)