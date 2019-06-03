package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.isImage
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class ReportVersionPageViewModel(@Serialise("reportJson") val report: ReportVersionDetails,
                                      val focalArtefactUrl: String?,
                                      val isAdmin: Boolean,
                                      val isRunner: Boolean,
                                      val isUsersManager: Boolean,
                                      val artefacts: List<ArtefactViewModel>,
                                      val dataLinks: List<InputDataViewModel>,
                                      val resources: List<DownloadableFileViewModel>,
                                      val zipFile: DownloadableFileViewModel,
                                      val versions: List<VersionPickerViewModel>,
                                      val changelog: List<ChangelogViewModel>,
                                      val appViewModel: AppViewModel) :
        AppViewModel by appViewModel
{
    constructor(report: ReportVersionDetails,
                focalArtefactUrl: String?,
                isAdmin: Boolean,
                isRunner: Boolean,
                isUsersManager: Boolean,
                artefacts: List<ArtefactViewModel>,
                dataLinks: List<InputDataViewModel>,
                resources: List<DownloadableFileViewModel>,
                zipFile: DownloadableFileViewModel,
                versions: List<VersionPickerViewModel>,
                changelog: List<ChangelogViewModel>,
                breadcrumbs: List<Breadcrumb>,
                loggedIn: Boolean,
                appName: String) :

            this(report,
                    focalArtefactUrl,
                    isAdmin,
                    isRunner,
                    isUsersManager,
                    artefacts,
                    dataLinks,
                    resources,
                    zipFile,
                    versions,
                    changelog,
                    DefaultViewModel(loggedIn, appName, breadcrumbs))

    companion object
    {
        fun build(report: ReportVersionDetails,
                  versions: List<String>,
                  changelog: List<Changelog>,
                  context: ActionContext): ReportVersionPageViewModel
        {
            val fileViewModelBuilder = ReportFileViewModelBuilder(report)

            val artefactViewModels = report.artefacts.map {
                buildArtefact(fileViewModelBuilder, it)
            }

            val focalArtefactUrl = getFocalArtefactUrl(fileViewModelBuilder, report.artefacts)

            val dataViewModels = report.dataHashes.map {
                InputDataViewModel(
                        it.key,
                        fileViewModelBuilder.buildDataFileViewModel(it.key, "csv"),
                        fileViewModelBuilder.buildDataFileViewModel(it.key, "rds"))
            }

            val resourceViewModels = report.resources.map {
                fileViewModelBuilder
                        .buildResourceFileViewModel(it)
            }

            val zipFile = fileViewModelBuilder
                    .buildZipFileViewModel()

            val isAdmin = context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))
            val isRunner = context.hasPermission(ReifiedPermission("reports.run", Scope.Global()))
            val isUsersManager = context.hasPermission(ReifiedPermission("users.manage", Scope.Global()))

            val displayName = report.displayName ?: report.name

            val breadcrumb = Breadcrumb("${report.name} (${report.id})", "/reports/${report.name}/${report.id}/")

            val changelogViewModel = changelog.groupBy { it.reportVersion }.map {
                ChangelogViewModel.build(it.key, it.value)
            }

            return ReportVersionPageViewModel(report.copy(displayName = displayName),
                    focalArtefactUrl,
                    isAdmin,
                    isRunner,
                    isUsersManager,
                    artefactViewModels,
                    dataViewModels,
                    resourceViewModels,
                    zipFile,
                    versions.map { buildVersionPickerViewModel(report.name, report.id, it) }.sortedByDescending { it.date },
                    changelogViewModel.sortedByDescending { it.date },
                    DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }

        private fun getFocalArtefactUrl(builder: ReportFileViewModelBuilder, artefacts: List<Artefact>): String?
        {
            //reproducing existing reportle behaviour - show the first artefact inline if it is possible
            val focalArtefactFile = if (artefacts.any() && artefacts[0].files.any() && canRenderInBrowser(artefacts[0].files[0]))
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
            val inlineFileName = getInlineFigureFile(artefact.files)
            val inline = if (inlineFileName == null)
            {
                null
            }
            else
            {
                fileBuilder
                        .inline()
                        .buildArtefactFileViewModel(inlineFileName)
                        .url
            }

            val files = artefact.files.map {
                fileBuilder.buildArtefactFileViewModel(it)
            }

            return ArtefactViewModel(artefact, files, inline)
        }

        private fun getInlineFigureFile(files: List<String>): String?
        {
            //reproducing existing reportle behaviour - show the first file inline if it is an image
            return if (files.count() > 0 && isImage(files[0]))
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
            return VersionPickerViewModel("/reports/$reportName/$id", date,
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

data class DownloadableFileViewModel(val name: String, val url: String)

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
