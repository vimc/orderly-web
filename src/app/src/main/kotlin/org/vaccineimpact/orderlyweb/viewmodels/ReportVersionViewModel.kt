package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.isImage
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

open class ReportVersionPageViewModel(@Serialise("reportJson") open val report: ReportVersionDetails,
                                      open val focalArtefactUrl: String?,
                                      open val isAdmin: Boolean,
                                      open val artefacts: List<ArtefactViewModel>,
                                      open val dataLinks: List<InputDataViewModel>,
                                      open val resources: List<DownloadableFileViewModel>,
                                      open val zipFile: DownloadableFileViewModel,
                                      override val breadcrumbs: List<Breadcrumb>,
                                      context: ActionContext) :
        AppViewModel(context)
{
    companion object
    {
        fun build(report: ReportVersionDetails, context: ActionContext): ReportVersionPageViewModel
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

            val breadcrumbs = listOf(IndexViewModel.breadcrumb,
                    Breadcrumb("${report.name} (${report.id})", fileViewModelBuilder.baseUrl))

            return ReportVersionPageViewModel(report.copy(displayName = displayName),
                    focalArtefactUrl,
                    isAdmin,
                    artefactViewModels,
                    dataViewModels,
                    resourceViewModels,
                    zipFile,
                    breadcrumbs,
                    context)
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

    }
}

data class ArtefactViewModel(val artefact: Artefact,
                             val files: List<DownloadableFileViewModel>,
                             val inlineArtefactFigure: String?)

data class InputDataViewModel(val key: String,
                              val csv: DownloadableFileViewModel,
                              val rds: DownloadableFileViewModel)

data class DownloadableFileViewModel(val name: String, val url: String)

