package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class ReportVersionViewModelBuilder(private val artefactViewModelBuilder: ArtefactViewModelBuilder
                                    = ArtefactViewModelBuilder())
{
    fun build(report: ReportVersionDetails, context: ActionContext): ReportVersionPageViewModel
    {
        val baseDownloadableFileViewModelBuilder = DownloadableFileViewModelBuilder()
                .withReport(report)

        val artefactViewModels = report.artefacts.map {
            artefactViewModelBuilder.build(report, it)
        }

        val focalArtefact = getInlineArtefactFile(report.artefacts)

        val focalArtefactUrl = if (focalArtefact == null)
        {
            null
        }
        else
        {
            baseDownloadableFileViewModelBuilder
                    .withFileName(focalArtefact)
                    .inline()
                    .buildArtefactFileViewModel()
                    .url
        }

        val dataViewModels = report.dataHashes.map {
            val builder = baseDownloadableFileViewModelBuilder
                    .withFileName(it.key)
            InputDataViewModel(
                    it.key,
                    builder.buildDataFileViewModel("csv"),
                    builder.buildDataFileViewModel("rds"))
        }

        val resourceViewModels = report.resources.map {
            baseDownloadableFileViewModelBuilder
                    .withFileName(it)
                    .buildResourceFileViewModel()
        }

        val zipFile = baseDownloadableFileViewModelBuilder
                .withFileName("${report.name}-${report.id}.zip")
                .buildZipFileViewModel()

        val isAdmin = context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))
        val displayName = report.displayName ?: report.name

        return ReportVersionPageViewModel(report.copy(displayName = displayName),
                focalArtefactUrl,
                isAdmin,
                artefactViewModels,
                dataViewModels,
                resourceViewModels,
                zipFile,
                context)
    }

    private fun getInlineArtefactFile(artefacts: List<Artefact>): String?
    {
        //reproducing existing reportle behaviour - show the first artefact inline if it is possible
        return if (artefacts.any() && artefacts[0].files.any() && canRenderInBrowser(artefacts[0].files[0]))
        {
            artefacts[0].files[0]
        }
        else
        {
            null
        }
    }
}
