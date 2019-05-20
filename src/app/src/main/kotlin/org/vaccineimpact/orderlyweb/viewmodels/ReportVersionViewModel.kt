package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.isImage
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.Instant

data class ReportVersionPageViewModel(@Serialise("reportJson") val report: ReportVersionDetails,
                                      val focalArtefactUrl: String?,
                                      val isAdmin: Boolean,
                                      val artefacts: List<ArtefactViewModel>,
                                      val dataLinks: List<InputDataViewModel>,
                                      val resources: List<DownloadableFileViewModel>,
                                      val zipFile: DownloadableFileViewModel,
                                      val versions: List<VersionPickerViewModel>,
                                      val appViewModel: AppViewModel) :
        AppViewModel by appViewModel
{
    constructor(report: ReportVersionDetails,
                focalArtefactUrl: String?,
                isAdmin: Boolean,
                artefacts: List<ArtefactViewModel>,
                dataLinks: List<InputDataViewModel>,
                resources: List<DownloadableFileViewModel>,
                zipFile: DownloadableFileViewModel,
                versions: List<VersionPickerViewModel>,
                breadcrumbs: List<Breadcrumb>,
                loggedIn: Boolean,
                appName: String) :

            this(report,
                    focalArtefactUrl,
                    isAdmin,
                    artefacts,
                    dataLinks,
                    resources,
                    zipFile,
                    versions,
                    DefaultViewModel(loggedIn, appName, breadcrumbs))

    companion object
    {
        fun build(report: ReportVersionDetails, versions: List<String>, context: ActionContext): ReportVersionPageViewModel
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
            val displayName = report.displayName ?: report.name

            val breadcrumb = Breadcrumb("${report.name} (${report.id})", "/reports/${report.name}/${report.id}/")

            return ReportVersionPageViewModel(report.copy(displayName = displayName),
                    focalArtefactUrl,
                    isAdmin,
                    artefactViewModels,
                    dataViewModels,
                    resourceViewModels,
                    zipFile,
                    versions.map { versionTimestamp(it) },
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

        private fun versionTimestamp(id: String): VersionPickerViewModel
        {
            val regex = Regex("/(d{4})(d{2})(d{2})-(d{2})(d{2})(d{2})-([0-9a-f]{8})/")
            val match = regex.matchEntire(id)
                    ?.groupValues!!

            return VersionPickerViewModel(id,
                    Instant.parse("${match[0]}-${match[1]}-${match[2]}T${match[3]}:${match[4]}:${match[5]}").toString())
        }

    }
}

data class VersionPickerViewModel(val id: String, val date: String)

data class ArtefactViewModel(val artefact: Artefact,
                             val files: List<DownloadableFileViewModel>,
                             val inlineArtefactFigure: String?)

data class InputDataViewModel(val key: String,
                              val csv: DownloadableFileViewModel,
                              val rds: DownloadableFileViewModel)

data class DownloadableFileViewModel(val name: String, val url: String)

